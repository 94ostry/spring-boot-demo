package com.postrowski.springbootdemo;

import com.postrowski.springbootdemo.model.Post;
import com.postrowski.springbootdemo.model.PostComment;
import com.postrowski.springbootdemo.model.PostDetails;
import com.postrowski.springbootdemo.service.PostRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PostRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void shouldCreatePostWithPostComments() {

        Post savePost = transactionTemplate.execute(status -> {
            Post post = Post.builder().title("Title1").build();

            PostComment comment1 = PostComment.builder().review("Comment during save 1").build();
            post.addComment(comment1);

            PostComment comment2 = PostComment.builder().review("Comment during save 2").build();
            post.addComment(comment2);

            return postRepository.save(post);
        });

        Assert.notNull(savePost, "Should be saved");
    }

    @Test
    public void rewriteMergedCollection() {
        transactionTemplate.execute(status -> {

            List<PostComment> comments = new ArrayList<>();

            PostComment comment1 = PostComment.builder().review("Comment during save 1").build();
            PostComment comment2 = PostComment.builder().review("Comment during save 2").build();

            comments.add(comment1);
            comments.add(comment2);

            Post post = Post.builder().title("Title1").comments(comments).build();
            postRepository.save(post);

            return null;
        });
    }

    @Test
    public void modifiedCollection() {
        Post detachedPost = transactionTemplate.execute(status -> {
            Post post = Post.builder().title("Title1").build();
            return postRepository.save(post);
        });

        transactionTemplate.execute(status -> {
            Post post = postRepository.findById(detachedPost.getId()).get();

            post.addComment(PostComment.builder().review("JDBC section is a must read!").build());
            post.addComment(PostComment.builder().review("The book size is larger than usual.").build());
            post.addComment(PostComment.builder().review("Just half-way through.").build());
            post.addComment(PostComment.builder().review("The book has over 450 pages.").build());

            postRepository.save(post);

            return null;
        });

        List<PostComment> deatachedComments = transactionTemplate.execute(status -> {

            List<PostComment> comments = entityManager.createQuery(
                    "select pc " +
                            "from PostComment pc " +
                            "join pc.post p " +
                            "where p.id = :postId " +
                            "order by pc.id", PostComment.class)
                    .setParameter("postId", detachedPost.getId())
                    .getResultList();

            comments.get(0).setReview("The JDBC part is a must have!");

            comments.remove(2);

            comments.add(PostComment.builder().review("The last part is about jOOQ and how to get the most of your relational database.").build());

            return comments;
        });

        //this throw deatached entity exception
//        transactionTemplate.execute(status -> {
//            postRepository.findById(detachedPost.getId()).ifPresent(post -> {
//                post.setComments(deatachedComments);
//            });
//
//            return null;
//        });

        //throws as no longer referenced by the owning entity instance:
//        transactionTemplate.execute(status -> {
//            Post post = postRepository.findById(detachedPost.getId()).get();
//            post.setComments(deatachedComments);
//
//            postRepository.save(post);
//
//            return null;
//        });


//        WORKING !!!!
//        transactionTemplate.execute(status -> {
//            Post post = entityManager
//                    .createQuery("select p from Post p join fetch p.comments where p.id = :id", Post.class)
//                    .setParameter("id", detachedPost.getId())
//                    .getSingleResult();
//
//            entityManager.detach(post);
//
//            post.getComments().clear();
//
//            for (PostComment comment : deatachedComments) {
//                post.addComment(comment);
//            }
//
//            entityManager.merge(post);
//
//            return null;
//        });
//        verifyResultsModifiedCollection();



        /*
        * It must remove the existing database records that are no longer found in the incoming collection.
        * It must update the existing database records which can be found in the incoming collection.
        * It must add the records found in the incoming collection, which cannot be found in the current database snapshot.
        */
        transactionTemplate.execute(status -> {

            Post post = entityManager
                    .createQuery("select p from Post p join fetch p.comments where p.id = :id", Post.class)
                    .setParameter("id", detachedPost.getId())
                    .getSingleResult();

            ArrayList<PostComment> removedComments = new ArrayList<>(post.getComments());
            removedComments.removeAll(deatachedComments);
            for (PostComment removedComment : removedComments) {
                post.removeComment(removedComment);
            }

            ArrayList<PostComment> newComments = new ArrayList<>(deatachedComments);
            newComments.removeAll(post.getComments());
            deatachedComments.removeAll(newComments);

            for(PostComment existingComment : deatachedComments) {
                existingComment.setPost(post);

                PostComment mergedComment = entityManager
                        .merge(existingComment);

                post.getComments().set(
                        post.getComments().indexOf(mergedComment),
                        mergedComment
                );
            }

            for(PostComment newComment : newComments) {
                post.addComment(newComment);
            }

            return null;
        });
        verifyResultsModifiedCollection();
    }

    private void verifyResultsModifiedCollection() {
        transactionTemplate.execute(status -> {
            Post post = entityManager.createQuery(
                    "select p " +
                            "from Post p " +
                            "join fetch p.comments c " +
                            "where p.id = :id " +
                            "order by c.id", Post.class)
                    .setParameter("id", 1L)
                    .getSingleResult();

            assertEquals(4, post.getComments().size());

            assertEquals(
                    "The JDBC part is a must have!",
                    post.getComments().get(0).getReview()
            );

            assertEquals(
                    "The book size is larger than usual.",
                    post.getComments().get(1).getReview()
            );

            assertEquals(
                    "The book has over 450 pages.",
                    post.getComments().get(2).getReview()
            );

            assertEquals(
                    "The last part is about jOOQ and how to get the most of your relational database.",
                    post.getComments().get(3).getReview()
            );

            return null;
        });
    }

    @Test
    public void shouldRemovePostComment() {
        Post savedPost = transactionTemplate.execute(status -> {
            Post post = Post.builder().title("Title1").build();

            PostComment comment1 = PostComment.builder().review("Comment during removing 1").build();
            post.addComment(comment1);

            PostComment comment2 = PostComment.builder().review("Comment during removing 2").build();
            post.addComment(comment2);

            return postRepository.save(post);
        });

        PostComment removedComment = transactionTemplate.execute(status -> {
            Optional<Post> postOptional = postRepository.findById(savedPost.getId());

            if (postOptional.isPresent()) {
                Post post = postOptional.get();

                PostComment comment = post.getComments().get(0);
                post.removeComment(comment);

                return comment;
            }

            return null;

        });

        Assert.notNull(removedComment, "Should be removed sth");
    }

    @Test
    public void shouldCreatePostDetails() {
        Post savedPost = transactionTemplate.execute(status -> {

            PostDetails postDetails = PostDetails.builder().createdBy("Ostry").createdOn(Instant.now()).build();

            Post post = Post.builder().title("Title1").build();
            post.setDetails(postDetails);

            return postRepository.save(post);
        });

        Assert.notNull(savedPost, "Should be saved");
    }


}
