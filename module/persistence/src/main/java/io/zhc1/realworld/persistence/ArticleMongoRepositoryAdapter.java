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
import io.zhc1.realworld.model.ArticleFavorite;
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
    private final ArticleTagMongoRepository articleTagMongoRepository;
    private final ArticleCommentMongoRepository articleCommentMongoRepository;
    private final ArticleFavoriteMongoRepository articleFavoriteMongoRepository; // Added
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public Article save(Article article) {
        return articleMongoRepository.save(article);
    }

    @Override
    @Transactional
    public Article save(Article article, Collection<Tag> tags) {
        Article savedArticle = articleMongoRepository.save(article);

        List<ArticleTag> oldArticleTags = articleTagMongoRepository.findByArticle(savedArticle);
        if (!oldArticleTags.isEmpty()) {
            articleTagMongoRepository.deleteAll(oldArticleTags);
        }
        savedArticle.getArticleTags().clear(); 

        if (tags != null) {
            for (Tag tagInput : tags) {
                Tag persistedTag = tagMongoRepository.save(tagInput); 
                ArticleTag newArticleTag = new ArticleTag(savedArticle, persistedTag);
                ArticleTag persistedArticleTag = articleTagMongoRepository.save(newArticleTag);
                savedArticle.getArticleTags().add(persistedArticleTag);
            }
        }
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
                orCriteriaList.add(Criteria.where("author").is(authorOpt.get()));
            } else {
                orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE)); 
            }
        }

        if (facets.tag() != null && !facets.tag().isBlank()) {
            Optional<Tag> tagOpt = tagMongoRepository.findById(facets.tag());
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
                         orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE));
                    }
                } else {
                    orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE));
                }
            } else {
                orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE));
            }
        }

        if (facets.favorited() != null && !facets.favorited().isBlank()) {
            Optional<User> favoritingUserOpt = userRepository.findByUsername(facets.favorited());
            if (favoritingUserOpt.isPresent()) {
                List<ArticleFavorite> favorites = articleFavoriteMongoRepository.findByUser(favoritingUserOpt.get());
                if (!favorites.isEmpty()) {
                    List<Integer> favoritedArticleIds = favorites.stream()
                            .map(fav -> fav.getArticle() != null ? fav.getArticle().getId() : null)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    if (!favoritedArticleIds.isEmpty()) {
                        orCriteriaList.add(Criteria.where("id").in(favoritedArticleIds));
                    } else {
                        orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE)); 
                    }
                } else {
                    orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE));
                }
            } else {
                orCriteriaList.add(Criteria.where("id").is(Integer.MIN_VALUE));
            }
        }

        if (!orCriteriaList.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
        }
        return mongoTemplate.find(query, Article.class);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return articleMongoRepository.findBySlug(slug);
    }

    @Override
    public List<Article> findByAuthors(Collection<User> authors, ArticleFacets facets) {
        PageRequest pageable = PageRequest.of(facets.page(), facets.size());
        return articleMongoRepository.findByAuthorInOrderByCreatedAtDesc(authors, pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetails findArticleDetails(Article article) {
        int totalFavorites = articleFavoriteMongoRepository.countByArticle(article);
        return ArticleDetails.unauthenticated(article, totalFavorites);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetails findArticleDetails(User requester, Article article) {
        int totalFavorites = articleFavoriteMongoRepository.countByArticle(article);
        boolean favorited = articleFavoriteMongoRepository.existsByUserAndArticle(requester, article);
        return new ArticleDetails(article, totalFavorites, favorited);
    }

    @Override
    @Transactional
    public void delete(Article article) {
        articleTagMongoRepository.deleteByArticle(article);
        articleCommentMongoRepository.deleteByArticle(article);
        articleFavoriteMongoRepository.deleteByArticle(article); // Added
        articleMongoRepository.delete(article);
    }

    @Override
    public boolean existsBy(String title) {
        return articleMongoRepository.existsByTitle(title);
    }
}
