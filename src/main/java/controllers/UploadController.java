/**
 * Copyright (C) 2012-2016 the original author or authors.
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

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import com.google.common.io.Files;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import dao.UserTableDao;
import etc.Globals;
import etc.PermissionType;
import etc.PostType;
import filters.LoginFilter;
import models.Post;
import models.UserTable;
import ninja.*;
import ninja.i18n.Lang;
import ninja.params.Param;
import ninja.session.Session;
import ninja.uploads.DiskFileItemProvider;
import ninja.uploads.FileProvider;
import ninja.utils.MimeTypes;
import org.slf4j.Logger;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.persistence.EntityManager;


@FileProvider(DiskFileItemProvider.class)
@Singleton
public class UploadController {

    /**
     * This is the system wide logger. You can still use any config you like. Or
     * create your own custom logger.
     *
     * But often this is just a simple solution:
     */
    @Inject
    public Logger logger;
    @Inject
    Provider<EntityManager> EntityManagerProvider;
    @Inject
    UserTableDao userTableDao;

    @Inject
    Lang lang;


    private static final String PROFILE_IMG_DIRECTORY = "/Users/xi/Sites/Database-Project_Techies.com/src/main/java/assets/img/general/";

    private static final String POST_IMG_DIRECTORY = "/Users/xi/Sites/Database-Project_Techies.com/src/main/java/assets/img/post/";

    private static final String FILE_SURFIX = ".jpg";

    private static final String POST_IMG_CONTENT = "Techies.com";

    private final MimeTypes mimeTypes;


    @Inject
    public UploadController(MimeTypes mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public Result upload() {
        // simply renders the default view for this controller
        return Results.html();
    }

    /**
     *
     * This upload method expects a file and simply displays the file in the
     * multipart upload again to the user (in the correct mime encoding).
     *
     * @param context
     * @return
     * @throws Exception
     */

    @Transactional
    @FilterWith(LoginFilter.class)
    public Result uploadFinish(Context context, @Param("upfile") File upfile) throws Exception { //to database
        UserTable actualUser = userTableDao.getUserFromSession(context);

        String absolutePath = PROFILE_IMG_DIRECTORY + actualUser.getId()+ FILE_SURFIX;

        File uploadedFile = new File(absolutePath);
        // saves the file to upload directory
        Files.copy(upfile, uploadedFile);
        return Results.redirect(Globals.PathProfile);
    }
    @Transactional
    @FilterWith(LoginFilter.class)
    public Result uploadPostImg (Context context, @Param("postImg") File postImg, @Param("permission") String permission) throws Exception {
        Session session = context.getSession();
        EntityManager em = EntityManagerProvider.get();

        UserTable actualUser = userTableDao.getUserFromSession(context);

        Post newImgPost = new Post(actualUser, PostType.Picture.ordinal(), POST_IMG_CONTENT, PermissionType.valueOf(permission).getValue(), new Timestamp(new Date().getTime()));

        em.persist(newImgPost);

        String absolutePath = POST_IMG_DIRECTORY + newImgPost.getId() + FILE_SURFIX;
        File uploadedFile = new File(absolutePath);
        // saves the file to upload directory
        Files.copy(postImg, uploadedFile);

        return Results.redirect(Globals.PathMainPage);
    }
}
