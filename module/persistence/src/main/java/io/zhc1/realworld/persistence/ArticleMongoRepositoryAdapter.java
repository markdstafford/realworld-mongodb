package io.zhc1.realworld.persistence;

import java.util.ArrayList;
import java.util.Collection;
// import java.util.HashSet; // Not strictly needed for the simplified version
import java.util.List;
import java.util.Optional;
// import java.util.Set; // Not strictly needed for the simplified version
// import java.util.stream.Collectors; // Not strictly needed for the simplified version

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
// import io.zhc1.realworld.model.ArticleTag; // Removed direct usage of ArticleTag instances for linking
import io.zhc1.realworld.model.Tag;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRepository;

@Profile("mongodb")
@Component("articleMongoRepositoryAdapter")
@RequiredArgsConstructor
class ArticleMongoRepositoryAdapter implements ArticleRepository {

    private final ArticleMongoRepository articleMongoRepository;
    private final TagMongoRepository tagMongoRepository;
    // private final ArticleTagMongoRepository articleTagMongoRepository; // Removed
    // private final ArticleCommentMongoRepository articleCommentMongoRepository; // Removed
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
        // If it's an update, existing article.getArticleTags() DBRefs will be preserved.
        Article savedArticle = articleMongoRepository.save(article);

        // 2. Ensure all provided Tag documents exist in the 'tags' collection.
        if (tags != null) {
            for (Tag tag : tags) {
                tagMongoRepository.save(tag); // Upserts tag by its name (ID)
            }
        }

        // TODO: Implement ArticleTag creation and persistence when ArticleTagMongoRepository is available.
        // This involves:
        //   a. For each Tag in the input 'tags':
        //      i. Create a new ArticleTag document.
        //      ii. Set its @DBRef to the persisted Tag.
        //      iii. Set its @DBRef to the savedArticle.
        //      iv. Save the ArticleTag document using ArticleTagMongoRepository.
        //   b. Clear savedArticle.getArticleTags() (if current behavior is to replace all tags).
        //   c. Add DBRefs of the newly saved ArticleTag documents to savedArticle.getArticleTags().
        //   d. Save 'savedArticle' again to update its list of @DBRef to ArticleTags.
        // For now, the article.getArticleTags() collection is NOT updated with new tags here.
        // New tags are persisted in the 'tags' collection, but not linked to the article.

        return savedArticle;
    }

    @Override
    public List<Article> findAll(ArticleFacets facets) {
        PageRequest pageable = PageRequest.of(facets.page(), facets.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Query query = new Query().with(pageable);
        List<Criteria> orCriteriaList = new ArrayList<>();

        if (facets.author() != null && !facets.author().isBlank()) {
            Optional<User> authorOpt = userRepository.findByUsername(facets.author());
            if (authorOpt.isPresent()) {
                // Query for articles where the 'author' field (DBRef) matches the found User's ID.
                // Spring Data MongoDB automatically handles DBRef resolution for direct fields.
                orCriteriaList.add(Criteria.where("author").is(authorOpt.get()));
            } else {
                // Author not found, this facet should result in no articles for the OR condition.
                // Add a criteria that will never match.
                orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE)); // Assuming ID is Integer
            }
        }

        if (facets.tag() != null && !facets.tag().isBlank()) {
            // TODO: Implement tag-based filtering once ArticleTag persistence and linking are complete.
            // This would involve querying articles whose 'articleTags' array contains a DBRef
            // to an ArticleTag document, which in turn contains a DBRef to a Tag document
            // with the name facets.tag().
            // Example conceptual query: Criteria.where("articleTags.tag.name").is(facets.tag())
            // For now, this facet is ignored to ensure compilation and basic functionality.
            System.err.println("TODO: Tag facet in ArticleMongoRepositoryAdapter.findAll is not yet implemented for MongoDB.");
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
        // TODO: Delete related ArticleComments using ArticleCommentMongoRepository when available.
        // Example: articleCommentMongoRepository.deleteByArticle(article);
        System.err.println("TODO: Deletion of related ArticleComments is not yet implemented for MongoDB.");

        // TODO: Delete related ArticleFavorites using ArticleFavoriteMongoRepository when available.
        // Example: articleFavoriteMongoRepository.deleteByArticle(article);
        System.err.println("TODO: Deletion of related ArticleFavorites is not yet implemented for MongoDB.");

        // TODO: Delete related ArticleTag documents using ArticleTagMongoRepository when available.
        // This would involve:
        //   1. Finding all ArticleTag documents that reference this article.
        //   2. Deleting those ArticleTag documents.
        System.err.println("TODO: Deletion of related ArticleTags is not yet implemented for MongoDB.");
        
        articleMongoRepository.delete(article);
    }

    @Override
    public boolean existsBy(String title) {
        return articleMongoRepository.existsByTitle(title);
    }
}
