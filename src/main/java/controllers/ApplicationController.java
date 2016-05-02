/**
 * Copyright (C) 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import dao.*;
import etc.PermissionType;
import models.*;
import ninja.Result;
import ninja.Results;

import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import etc.Globals;
import etc.PostType;
import etc.RelationType;
import filters.LoginFilter;
import java.sql.Timestamp;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import ninja.Context;
import ninja.FilterWith;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.session.Session;
import ninja.uploads.DiskFileItemProvider;
import ninja.uploads.FileProvider;

@FileProvider(DiskFileItemProvider.class)
@Singleton
public class ApplicationController {
    @Inject
    Provider<EntityManager> EntityManagerProvider;
    @Inject UserTableDao userTableDao;
    @Inject RelationshipDao relationshipDao;
    @Inject PostDao postDao;
    @Inject CommentDao commentDao;
    @Inject DiaryDao diaryDao;
    @Inject MailController mailController;
    @Inject ProfileDao profileDao;
    @Inject DiaryCommentDao diaryComment;

    @FilterWith(LoginFilter.class)
    public Result index(Context context) {
        // Redirect to "news" because filter is OK
        return Results.redirect(Globals.PathMainPage);
    }

    @FilterWith(LoginFilter.class)
    public Result news(Context context) {
        // Initial declarations
        Result html = Results.html();
        EntityManager em = EntityManagerProvider.get();
        Session session = context.getSession();

        // Temporal vars
        // -------------
        UserTable actualUser = userTableDao.getUserFromSession(context);
        // Get mutual friend list
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        List<UserTable> friendsOfFriends = relationshipDao.getFOFlist(mutualFriends, RelationType.Friends);
        Set<UserTable> fAndFOF = new HashSet<>();
        fAndFOF.addAll(mutualFriends);
        fAndFOF.addAll(friendsOfFriends);

        List<UserTable> userTempList = new ArrayList<>();
        userTempList.add(actualUser);
        List<Post> posts = new ArrayList<>();
        List<Post> userPosts = postDao.getPostsFromUsers(userTempList);

        // Get mutual friends
        //TODO: get mutual friend's post, reveal if is not "PRIVATE(3)" otherwise not reveal
        if(mutualFriends.size() != 0) {
            List<Post> friendsPosts = postDao.getPostsFromUsersWithPermission(mutualFriends, PermissionType.PRIVATE.getValue());
            posts.addAll(friendsPosts);
        }
        //TODO: get FOF's post
        //friendsOfFriends = new ArrayList<UserTable>();
        //friendsOfFriends.addAll(fAndFOF);
        if(friendsOfFriends.size() != 0) {
            List<Post> fofPosts = postDao.getPostsFromUsersWithPermission(friendsOfFriends, PermissionType.FRIENDS.getValue());
            posts.addAll(fofPosts);
        }
        posts.addAll(userPosts);
        //TODO: odered all the posts by timestamp
        if(posts.size() != 0) {
            posts = postDao.orderPostsByTimestamp(posts);
        }
        List<Comment> comments = commentDao.getCommentsByPosts(posts);
        // HTML Rendering stuff
        html.render("user", actualUser);
        html.render("posts", posts);
        html.render("comments", comments);
        html.render("friends", mutualFriends);
        List<Diary> diaries = new ArrayList<>();
        List<DiaryComment> diaryComments=new ArrayList<>();
        if(mutualFriends.size() != 0) {
            diaries = diaryDao.getDiaryFromUsers(mutualFriends);
            diaryComments=diaryComment.getCommentsByPosts(diaries);
        }
            html.render("diarys", diaries);
            html.render("diarycomments",diaryComments);
        return html;
    }

    @Transactional
    public Result login(@Param("email") String pEmail, @Param("secret") String pPassword, Context context) {
        if("POST".equals(context.getMethod()))
        {
           UserTable canLogin = userTableDao.canLogin(pEmail, pPassword);

            if (canLogin != null) {
                EntityManager em = EntityManagerProvider.get();

                User_session uSession = new User_session(canLogin);
                em.persist(uSession);
                context.getSession().put(Globals.CookieSession, uSession.getId());
                return Results.redirect(Globals.PathMainPage);
            }
        } else {
            //return Results.redirect(Globals.PathMainPage);
            return Results.html();
        }
        //return Results.redirect(Globals.PathMainPage);
        return Results.redirect(Globals.PathRoot);
    }

    public Result logout(Context context)
    {
        context.getSession().clear();

        return Results.redirect(Globals.PathRoot);
    }
    
    //redirect to news after register
    public Result register(@Param("email") String pEmail,
                           @Param("secret") String pPassword,
                           @Param("fullname") String pFullName,
                           @Param("username") String pUsername,
                           Context context) {
        boolean emailExist = userTableDao.emailExist(pEmail);
        if (emailExist) {
            return Results.redirect(Globals.PathRoot);
        } else {
            Session session = context.getSession();
            EntityManager em = EntityManagerProvider.get();

            UserTable user = new UserTable(pUsername, pEmail, pPassword, pFullName);

            em.persist(user);

            UserTable canLogin = userTableDao.canLogin(pEmail, pPassword);

            if (canLogin != null) {
                User_session uSession = new User_session(canLogin);
                em.persist(uSession);
                context.getSession().put(Globals.CookieSession, uSession.getId());
                Profile profile = new Profile(uSession.getUser(), "This guy is lazy, he did not wirte anything!"," "," "," "," "," ");
                em.persist(profile);
                return Results.redirect(Globals.PathProfile);
            } else {
                //return Results.redirect(Globals.PathMainPage);
                return Results.html();

            }
        }
    }


    @Transactional
    @FilterWith(LoginFilter.class)
    public Result post_create (@Param("content") String content, @Param("permission") String permission, Context context) {
        //System.out.print("BEGINING test");
        Session session = context.getSession();
        EntityManager em = EntityManagerProvider.get();

       UserTable actualUser = userTableDao.getUserFromSession(context);
        //System.out.print("actual User");

        Post newPost = new Post(actualUser, PostType.Status.ordinal(), content, PermissionType.valueOf(permission).getValue(), new Timestamp(new Date().getTime()));
        //System.out.print("TEST, timestamp: " + newPost.getTimestamp());
        em.persist(newPost);

        return Results.redirect(Globals.PathRoot);
    }

    @Transactional
    @FilterWith(LoginFilter.class)
    public Result post_comment (@Param("post") String Post, @Param("content") String Content, @Param("returnto") String returnto, Context context) {
        Session session = context.getSession();
        EntityManager em = EntityManagerProvider.get();

       UserTable user = userTableDao.getUserFromSession(context);

        Comment newComment = new Comment(user, Long.valueOf(Post), Content, new Timestamp(new Date().getTime()));
        em.persist(newComment);

        if(returnto == null)
            return Results.redirect(Globals.PathRoot);
        else
            return Results.redirect(returnto + "#comment_" + newComment.getId());
    }

    @FilterWith(LoginFilter.class)
    public Result profile (Context context) {
        // Initial declarations
        Result html = Results.html();

       UserTable actualUser = userTableDao.getUserFromSession(context);
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        List<Relationship> friendRequest = relationshipDao.getFriendRequests(actualUser);

        html.render("user", actualUser);
        html.render("friends", mutualFriends);
        html.render("requests", friendRequest);

        return html;
    }

    @FilterWith(LoginFilter.class)
    public Result friend_add(@PathParam("username") String pUsername, Context context) {
       UserTable actualUser = userTableDao.getUserFromSession(context);
       UserTable target = userTableDao.getUserFromUsername(pUsername);
        Relationship relation = relationshipDao.getRelationByUsername(actualUser, target);

        if(relation == null) {
            relationshipDao.createNewRelation(actualUser, target);
            mailController.sendMail(target, context);
            return Results.redirect(Globals.PathProfileView + target.getId());
        }
        return Results.redirect(Globals.PathError);
    }

    @Transactional
    @FilterWith(LoginFilter.class)
    public Result friend_accept (@PathParam("relid") Long relID, Context context) {
        EntityManager em = EntityManagerProvider.get();
       UserTable user = userTableDao.getUserFromSession(context);

        Query q = em.createQuery("SELECT x FROM Relationship x WHERE relation_id =:relid");
        q.setParameter("relid", relID);
        List<Relationship> relation = (List<Relationship>) q.getResultList();

        if(relation.size() == 1) {
            if(Objects.equals(relation.get(0).getUser_b().getId(), user.getId())) {
                relation.get(0).setRelation_type(RelationType.Friends.ordinal());
                em.merge(relation.get(0));

                return Results.redirect(Globals.PathProfile);
            }
        } //TODO: Else, mostrar un error
        return Results.redirect(Globals.PathRoot);
    }

    @Transactional
    @FilterWith(LoginFilter.class)
    public Result friend_reject (@PathParam("relid") Long relID, Context context) {
        EntityManager em = EntityManagerProvider.get();
       UserTable user = userTableDao.getUserFromSession(context);
        Relationship relation = relationshipDao.getRelationByID(relID);

        if(relation != null) {
            if(Objects.equals(relation.getUser_b().getId(), user.getId())) {
                em.remove(relation);

                return Results.redirect(Globals.PathProfile);
            }
        }
        return Results.redirect(Globals.PathRoot);
    }

    @Transactional
    @FilterWith(LoginFilter.class)
    public Result profile_set (@Param("content") String status, Context context) {
        EntityManager em = EntityManagerProvider.get();
       UserTable user = userTableDao.getUserFromSession(context);

        user.setStatus(status);

        return Results.redirect(Globals.PathProfile);
    }
    @Transactional
    @FilterWith(LoginFilter.class)
    public Result profile_view(@PathParam("userid") Long userid, Context context) {
        // Initial declarations
        Result html = Results.html();

        UserTable actualUser = userTableDao.getUserFromSession(context);
        UserTable targetUser = userTableDao.getUserFromUserid(userid);
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        Relationship relationship = relationshipDao.getRelationByUsername(actualUser, targetUser);
        Profile profile= profileDao.getProfileFromProfile(targetUser);

        List<Diary> diary = diaryDao.getDiaryFromUsers(targetUser);
        List<DiaryComment> diaryComments=diaryComment.getCommentsByPosts(diary);
        html.render("diary", diary);

        html.render("diarycomments",diaryComments);
        boolean disable_add = false;

        if(relationship != null) {
            if (relationship.getRelation_type() == RelationType.Friends.ordinal() || relationship.getRelation_type() == RelationType.Request.ordinal()) {
                html.render("relation", relationship);
                disable_add = (relationship.getRelation_type() == RelationType.Request.ordinal()) && Objects.equals(relationship.getUser_a().getId(), actualUser.getId());

                if(relationship.getRelation_type() == RelationType.Friends.ordinal()) {
                    // Get mutual friends post
                    List<Post> posts = postDao.getPostsFromUsers(new ArrayList<>(Arrays.asList(targetUser)));
                    //List<Comment> comments
                    List<Comment> comments = commentDao.getCommentsByPosts(posts);

                    html.render("posts", posts);
                    html.render("comments", comments);

                }
            } else {
                return Results.redirect(Globals.PathError);
            }
        } else {
            html.render("relation", new Relationship(actualUser, targetUser, RelationType.None.ordinal()));
        }

        html.render("user", actualUser);
        html.render("target", targetUser);
        html.render("friends", mutualFriends);
        html.render("disable_add", disable_add);
        html.render("profile",profile);

        return html;
    }
    @FilterWith(LoginFilter.class)
    public Result search_result(@Param("keyword") String keyword, Context context) {
        // Initial declarations
        Result html = Results.html();


        EntityManager em = EntityManagerProvider.get();
        Session session = context.getSession();

        // Temporal vars
        // -------------
        UserTable actualUser = userTableDao.getUserFromSession(context);

        // Get mutual friend list
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        mutualFriends.add(actualUser);



        List<UserTable>  userResult = userTableDao.getUserListFromKeyword(keyword);


        List<Post> postResult = postDao.getPostFromKeyword(keyword);

        List<Diary> diary = diaryDao.getDiaryFromKeyword(keyword);
        // HTML Rendering stuff
        html.render("diary",diary);
        html.render("user", actualUser);
        html.render("friends", mutualFriends);
        html.render("userResult", userResult);
        html.render("postResult", postResult);

        return html;
        //return Results.redirect(Globals.PathProfile);
    }
    @FilterWith(LoginFilter.class)
    public Result post_view(@PathParam("postid") Long postid, Context context) {
        // Initial declarations
        Result html = Results.html();

        UserTable actualUser = userTableDao.getUserFromSession(context);
        //UserTable targetUser = userTableDao.getUserFromUsername(post);
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        //Relationship relationship = relationshipDao.getRelationByUsername(actualUser, targetUser);

        Post post = postDao.getPostFromSearchResult(postid);
        //List<Comment> comments
        List<Comment> comments = commentDao.getCommentsBySearchresult(post);

        html.render("comments", comments);
        html.render("user", actualUser);
        html.render("post", post);
        html.render("friends", mutualFriends);

        return html;
    }
    @Transactional
    @FilterWith(LoginFilter.class)
    public Result showdiary(@PathParam("diaryid") Long diary_id, Context context) {
        // Initial declarations
        Result html = Results.html();

        UserTable actualUser = userTableDao.getUserFromSession(context);
        //UserTable targetUser = userTableDao.getUserFromUsername(post);
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        //Relationship relationship = relationshipDao.getRelationByUsername(actualUser, targetUser);

        Diary diary = diaryDao.getDiaryFromSearchResult(diary_id);
        List<DiaryComment> diarycomments=diaryComment.getCommentsBydiary(diary);
        html.render("diary", diary);

        html.render("diarycomments",diarycomments);
        html.render("user", actualUser);
        html.render("friends", mutualFriends);

        return html;
    }

    @Transactional
    @FilterWith(LoginFilter.class)
    public Result diary_create (@Param("content") String content, @Param("title")String title, Context context) {
        //System.out.print("BEGINING test");
        Result html = Results.html();
        //Relationship relationship = relationshipDao.getRelationByUsername(actualUser, targetUser);

        Session session = context.getSession();
        EntityManager em = EntityManagerProvider.get();
        UserTable actualUser = userTableDao.getUserFromSession(context);
        //System.out.print("actual User");
        Diary newDiary= new Diary(actualUser,content, title ,new Timestamp(new Date().getTime()));
        em.persist(newDiary);
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        Diary diary = diaryDao.getDiaryFromSearchResult(newDiary.getId());




        html.render("diary", diary);
        html.render("user", actualUser);

        html.render("friends", mutualFriends);

        return html;
    }

    //@Transactional
    @FilterWith(LoginFilter.class)
    public Result diary_create_view (Context context) {
        Result html = Results.html();
        EntityManager em = EntityManagerProvider.get();
        Session session = context.getSession();

        // Temporal vars
        // -------------
        UserTable actualUser = userTableDao.getUserFromSession(context);

        // Get mutual friend list
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        mutualFriends.add(actualUser);

        html.render("user", actualUser);
        html.render("friends", mutualFriends);

        return html;
    }

    @Transactional
    @FilterWith(LoginFilter.class)
    public Result profile_create (Context context, @Param("birthday") String birthday, @Param("gender")String gender, @Param("hobby") String hobby,@Param("marital_status")String marital_status, @Param("work_place")String work_place,@Param("helper")String helper) {

        Result html = Results.html();

        Session session = context.getSession();
        EntityManager em = EntityManagerProvider.get();
        UserTable actualUser = userTableDao.getUserFromSession(context);

        Profile newProfile= new Profile(actualUser,birthday,gender,hobby,marital_status,work_place,helper);

        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);


        if(profileDao.getProfileFromProfile(actualUser)!=null) {
            profileDao.getProfileFromProfile(actualUser).setAuthor(actualUser);
            profileDao.getProfileFromProfile(actualUser).setBirthday(birthday);
            profileDao.getProfileFromProfile(actualUser).setGender(gender);
            profileDao.getProfileFromProfile(actualUser).setHobby(hobby);
            profileDao.getProfileFromProfile(actualUser).setMarital_status(marital_status);
            profileDao.getProfileFromProfile(actualUser).setHelper(helper);
            profileDao.getProfileFromProfile(actualUser).setWork_place(work_place);
            em.persist(profileDao.getProfileFromProfile(actualUser));
        }
        else{
            em.persist(newProfile);
        }

        html.render("user", actualUser);
        html.render("friends", mutualFriends);
        html.render("profile",newProfile);


        return html;


    }
    public Result self_profile_view(Context context) {
        // Initial declarations
        Result html = Results.html();

        UserTable actualUser = userTableDao.getUserFromSession(context);
        List<UserTable> mutualFriends = relationshipDao.getRelationList(actualUser, RelationType.Friends);
        Profile profile= profileDao.getProfileFromProfile(actualUser);

        html.render("user", actualUser);
        html.render("friends", mutualFriends);
        html.render("profile",profile);

        return html;
    }
    @Transactional
    @FilterWith(LoginFilter.class)
    public Result post_diary_comment (@Param("diary") String Post, @Param("content") String Content, @Param("returnto") String returnto, Context context) {
        Session session = context.getSession();
        EntityManager em = EntityManagerProvider.get();

        UserTable user = userTableDao.getUserFromSession(context);

        DiaryComment newComment = new DiaryComment(user, Long.valueOf(Post), Content, new Timestamp(new Date().getTime()));
        em.persist(newComment);

        if(returnto == null)
            return Results.redirect(Globals.PathRoot);
        else
            return Results.redirect(returnto + "#comment_" + newComment.getId());
    }

}
