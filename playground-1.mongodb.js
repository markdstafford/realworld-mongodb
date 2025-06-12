// migration/mongodb-sample-data.js
// MongoDB sample data script for RealWorld application
// This script creates collections and populates them with sample data

// Connect to the realworld database (creates it if it doesn't exist)
const dbName = 'realworld';
print(`Connecting to database: ${dbName}`);
db = db.getSiblingDB(dbName);

// Drop existing collections to start fresh
print('Dropping existing collections...');
db.users.drop();
db.article.drop();
db.articleFavorite.drop();
db.articleTag.drop();
db.articleComment.drop();
db.tag.drop();
db.userFollow.drop();

// Create users collection with sample data
print('Creating users collection...');
const users = [
  {
    _id: ObjectId(),
    email: 'jane@example.com',
    username: 'jane',
    password: '$2a$10$8KzaNdKwIYwtNgMjG/IR9u.Cm1Vj9rKPPGiJup.O6nqfRxJoqkQZa', // hashed "password"
    bio: 'I work at the coffee shop',
    imageUrl: 'https://api.realworld.io/images/demo-avatar.png',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    email: 'john@example.com',
    username: 'john',
    password: '$2a$10$8KzaNdKwIYwtNgMjG/IR9u.Cm1Vj9rKPPGiJup.O6nqfRxJoqkQZa', // hashed "password"
    bio: 'Software developer and tech enthusiast',
    imageUrl: 'https://api.realworld.io/images/smiley-cyrus.jpeg',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    email: 'mary@example.com',
    username: 'mary',
    password: '$2a$10$8KzaNdKwIYwtNgMjG/IR9u.Cm1Vj9rKPPGiJup.O6nqfRxJoqkQZa', // hashed "password"
    bio: 'Writer and blogger',
    imageUrl: null,
    createdAt: new Date()
  }
];

// Insert users
db.users.insertMany(users);
print(`Inserted ${users.length} users`);

// Create tags collection with sample data
print('Creating tag collection...');
const tags = [
  {
    _id: ObjectId(),
    name: 'mongodb',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    name: 'java',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    name: 'spring',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    name: 'webdev',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    name: 'programming',
    createdAt: new Date()
  }
];

// Insert tags
db.tag.insertMany(tags);
print(`Inserted ${tags.length} tags`);

// Create articles collection with sample data
print('Creating article collection...');
const articles = [
  {
    _id: ObjectId(),
    createdAt: new Date(),
    updatedAt: new Date(),
    authorId: users[0]._id, // Jane's ID
    description: 'How to use MongoDB with Spring Boot',
    slug: 'how-to-use-mongodb-with-spring-boot',
    title: 'MongoDB with Spring Boot',
    content: 'This is a detailed guide on integrating MongoDB with Spring Boot applications. MongoDB is a document database that offers high performance, high availability, and easy scalability.'
  },
  {
    _id: ObjectId(),
    createdAt: new Date(new Date().getTime() - 86400000), // 1 day ago
    updatedAt: new Date(new Date().getTime() - 86400000),
    authorId: users[1]._id, // John's ID
    description: 'Introduction to Java 21 features',
    slug: 'introduction-to-java-21-features',
    title: 'Java 21 Features',
    content: 'Java 21 brings exciting new features including virtual threads, pattern matching for switch expressions, record patterns, and more. This article explores these features with practical examples.'
  },
  {
    _id: ObjectId(),
    createdAt: new Date(new Date().getTime() - 172800000), // 2 days ago
    updatedAt: new Date(new Date().getTime() - 172800000),
    authorId: users[2]._id, // Mary's ID
    description: 'Building RESTful APIs with Spring Boot',
    slug: 'building-restful-apis-with-spring-boot',
    title: 'RESTful APIs with Spring Boot',
    content: 'This article demonstrates how to build production-ready RESTful APIs using Spring Boot. We\'ll cover request handling, response formatting, error handling, and documentation.'
  }
];

// Insert articles
db.article.insertMany(articles);
print(`Inserted ${articles.length} articles`);

// Create articleTag collection with sample data
print('Creating articleTag collection...');
const articleTags = [
  {
    _id: ObjectId(),
    articleId: articles[0]._id,
    tagName: tags[0].name, // mongodb
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[0]._id,
    tagName: tags[2].name, // spring
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[1]._id,
    tagName: tags[1].name, // java
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[2]._id,
    tagName: tags[2].name, // spring
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[2]._id,
    tagName: tags[3].name, // webdev
    createdAt: new Date()
  }
];

// Insert articleTags
db.articleTag.insertMany(articleTags);
print(`Inserted ${articleTags.length} article tags`);

// Create articleComment collection with sample data
print('Creating articleComment collection...');
const articleComments = [
  {
    _id: ObjectId(),
    articleId: articles[0]._id,
    authorId: users[1]._id, // John's comment on Jane's article
    content: 'Great article! I learned a lot about MongoDB integration.',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[0]._id,
    authorId: users[2]._id, // Mary's comment on Jane's article
    content: 'Would love to see more examples of complex queries.',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[1]._id,
    authorId: users[0]._id, // Jane's comment on John's article
    content: 'Virtual threads are a game changer!',
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[2]._id,
    authorId: users[1]._id, // John's comment on Mary's article
    content: 'Very helpful guide for API development.',
    createdAt: new Date()
  }
];

// Insert articleComments
db.articleComment.insertMany(articleComments);
print(`Inserted ${articleComments.length} article comments`);

// Create articleFavorite collection with sample data
print('Creating articleFavorite collection...');
const articleFavorites = [
  {
    _id: ObjectId(),
    articleId: articles[0]._id,
    userId: users[1]._id, // John favorited Jane's article
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[0]._id,
    userId: users[2]._id, // Mary favorited Jane's article
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    articleId: articles[1]._id,
    userId: users[0]._id, // Jane favorited John's article
    createdAt: new Date()
  }
];

// Insert articleFavorites
db.articleFavorite.insertMany(articleFavorites);
print(`Inserted ${articleFavorites.length} article favorites`);

// Create userFollow collection with sample data
print('Creating userFollow collection...');
const userFollows = [
  {
    _id: ObjectId(),
    followerId: users[1]._id, // John follows Jane
    followingId: users[0]._id,
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    followerId: users[2]._id, // Mary follows Jane
    followingId: users[0]._id,
    createdAt: new Date()
  },
  {
    _id: ObjectId(),
    followerId: users[0]._id, // Jane follows John
    followingId: users[1]._id,
    createdAt: new Date()
  }
];

// Insert userFollows
db.userFollow.insertMany(userFollows);
print(`Inserted ${userFollows.length} user follows`);

// Create indexes to improve query performance
print('Creating indexes...');
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "username": 1 }, { unique: true });
db.article.createIndex({ "slug": 1 }, { unique: true });
db.article.createIndex({ "authorId": 1 });
db.articleFavorite.createIndex({ "userId": 1, "articleId": 1 }, { unique: true });
db.articleTag.createIndex({ "articleId": 1, "tagName": 1 }, { unique: true });
db.articleComment.createIndex({ "articleId": 1 });
db.userFollow.createIndex({ "followerId": 1, "followingId": 1 }, { unique: true });

print('Sample data creation completed successfully!');
print('Run this script with: mongo mongodb://localhost:27017/admin migration/mongodb-sample-data.js');
