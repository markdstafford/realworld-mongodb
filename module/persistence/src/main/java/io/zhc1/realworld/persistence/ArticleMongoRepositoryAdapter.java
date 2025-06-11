package io.zhc1.realworld.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.ArticleDetails;
import io.zhc1.realworld.model.ArticleFacets;
// import io.zhc1.realworld.model.ArticleFavorite; // Removed dependency
import io.zhc1.realworld.model.ArticleRepository;
import io.zhc1.realworld.model.ArticleTag;
import io.zhc1.realworld.model.Tag;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRepository;

@Profile("mongodb")
@Component("articleMongoRepositoryAdapter")
@RequiredArgsConstructor
class ArticleMongoRepositoryAdapter implements ArticleRepository {

    private final ArticleMongoRepository articleMongoRepository;
    private final TagMongoRepository tagMongoRepository;
    private final ArticleTagMongoRepository articleTagMongoRepository; // Added dependency
    private final ArticleCommentMongoRepository articleCommentMongoRepository; // Uncommented and added to constructor
    // private final ArticleFavoriteMongoRepository articleFavoriteMongoRepository; // Removed
    private final UserRepository userRepository; // Used to find user for 'author' facet
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public Article save(Article article) {
        // Author should be a managed entity before saving an article that references it.
        // If author is new, it should be saved via UserRepository first.
        // Assuming author in the article object is already persisted or will be handled by cascading if applicable.
        return articleMongoRepository.save(article);
    }

    @Override
    @Transactional
    public Article save(Article article, Collection<Tag> tags) {
        // 1. Save the article's core properties.
        Article savedArticle = articleMongoRepository.save(article);

        // 2. Clear existing tag associations for this article
        List<ArticleTag> oldArticleTags = articleTagMongoRepository.findByArticle(savedArticle);
        if (!oldArticleTags.isEmpty()) {
            articleTagMongoRepository.deleteAll(oldArticleTags);
        }
        savedArticle.getArticleTags().clear(); // Clear the collection on the entity

        // 3. Create and persist new ArticleTag associations
        if (tags != null) {
            for (Tag tagInput : tags) {
                Tag persistedTag = tagMongoRepository.save(tagInput); // Upserts tag by its name (ID)
                ArticleTag newArticleTag = new ArticleTag(savedArticle, persistedTag);
                ArticleTag persistedArticleTag = articleTagMongoRepository.save(newArticleTag);
                // The Article.addTag method internally adds to the Set and sets the back-reference.
                // However, since we are managing DBRefs, we add the persisted ArticleTag
                // which now has its own ID and correct DBRefs to Article and Tag.
                savedArticle.getArticleTags().add(persistedArticleTag);
            }
        }

        // 4. Save the article again to update its list of @DBRef to ArticleTags.
        return articleMongoRepository.save(savedArticle);
    }

    @Override
    public List<Article> findAll(ArticleFacets facets) {
        PageRequest pageable = PageRequest.of(facets.page(), facets.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Query query = new Query().with(pageable);
        List<Criteria> orCriteriaList = new ArrayList<>();

        if (facets.author() != null && !facets.author().isBlank()) {
            Optional<User> authorOpt = userRepository.findByUsername(facets.author());
            if (authorOpt.isPresent()) {
                // Query for articles where the 'author' field (DBRef) matches the found User.
                // Spring Data MongoDB automatically handles DBRef resolution for direct fields.
                orCriteriaList.add(Criteria.where("author").is(authorOpt.get()));
            } else {
                // Author not found, this facet should result in no articles for the OR condition.
                orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE)); 
            }
        }

        if (facets.tag() != null && !facets.tag().isBlank()) {
            Optional<Tag> tagOpt = tagMongoRepository.findById(facets.tag()); // Tag name is the ID
            if (tagOpt.isPresent()) {
                Tag persistedTag = tagOpt.get();
                List<ArticleTag> articleTagsWithGivenTag = articleTagMongoRepository.findByTag(persistedTag);
                if (!articleTagsWithGivenTag.isEmpty()) {
                    List<Integer> articleIds = articleTagsWithGivenTag.stream()
                            .map(at -> at.getArticle() != null ? at.getArticle().getId() : null)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    if (!articleIds.isEmpty()) {
                        orCriteriaList.add(Criteria.where("id").in(articleIds));
                    } else {
                        // Tag exists, but no articles are associated with it via ArticleTag
                         orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE));
                    }
                } else {
                    // Tag exists, but no ArticleTag documents link to it
                    orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE));
                }
            } else {
                // Tag itself doesn't exist
                orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE));
            }
        }

        if (facets.favorited() != null && !facets.favorited().isBlank()) {
            // TODO: Implement favorited-based filtering once ArticleFavorite persistence is complete.
            // This would involve finding the user by 'facets.favorited()',
            // then finding all ArticleFavorite documents for that user,
            // then finding all Articles whose IDs are in the 'article' DBRefs of those ArticleFavorites.
            System.err.println("TODO: Favorited facet in ArticleMongoRepositoryAdapter.findAll is not yet implemented for MongoDB.");
        }

        if (!orCriteriaList.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
        }
        // If orCriteriaList is empty (no implemented/matched facets), query fetches all articles paginated.

        return mongoTemplate.find(query, Article.class);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return articleMongoRepository.findBySlug(slug);
    }

    @Override
    public List<Article> findByAuthors(Collection<User> authors, ArticleFacets facets) {
        PageRequest pageable = PageRequest.of(facets.page(), facets.size());
        // This relies on the derived query in ArticleMongoRepository.
        // Ensure 'authors' contains User objects that can be matched by DBRef.
        return articleMongoRepository.findByAuthorInOrderByCreatedAtDesc(authors, pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetails findArticleDetails(Article article) {
        int totalFavorites = 0; // Changed from long to int
        // TODO: Implement totalFavorites calculation once ArticleFavoriteMongoRepository is available.
        // Example: totalFavorites = articleFavoriteMongoRepository.countByArticle(article);
        System.err.println("TODO: totalFavorites in ArticleMongoRepositoryAdapter.findArticleDetails is not yet implemented for MongoDB.");
        return ArticleDetails.unauthenticated(article, totalFavorites);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetails findArticleDetails(User requester, Article article) {
        int totalFavorites = 0; // Changed from long to int
        boolean favorited = false;
        // TODO: Implement totalFavorites and favorited status once ArticleFavoriteMongoRepository is available.
        // Example: totalFavorites = articleFavoriteMongoRepository.countByArticle(article);
        // Example: favorited = articleFavoriteMongoRepository.existsByUserAndArticle(requester, article);
        System.err.println("TODO: totalFavorites and favorited status in ArticleMongoRepositoryAdapter.findArticleDetails is not yet implemented for MongoDB.");
        return new ArticleDetails(article, totalFavorites, favorited);
    }

    @Override
    @Transactional
    public void delete(Article article) {
        // Delete related ArticleTag associations
        articleTagMongoRepository.deleteByArticle(article);

        // Delete related ArticleComments
        articleCommentMongoRepository.deleteByArticle(article);

        // TODO: Delete related ArticleFavorites using ArticleFavoriteMongoRepository when available.
        // Example: articleFavoriteMongoRepository.deleteByArticle(article);
        System.err.println("TODO: Deletion of related ArticleFavorites is not yet implemented for MongoDB.");
        
        articleMongoRepository.delete(article);
    }

    @Override
    public boolean existsBy(String title) {
        return articleMongoRepository.existsByTitle(title);
    }
}
