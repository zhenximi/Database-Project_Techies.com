package controllers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import dao.UserTableDao;
import models.UserTable;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import ninja.uploads.DiskFileItemProvider;
import ninja.uploads.FileProvider;
import org.apache.commons.mail.EmailException;

import javax.mail.internet.AddressException;

import ninja.Context;



/**
 * Created by zmi on 4/24/16.
 */

@FileProvider(DiskFileItemProvider.class)
@Singleton
public class MailController {

    @Inject
    Provider<Mail> mailProvider;

    @Inject
    Postoffice postoffice;

    @Inject
    UserTableDao userTableDao;

    private static final String EMAIL_BODY = "http://localhost:8080/";

    public void sendMail(UserTable target, Context context) {
        UserTable actualUser = userTableDao.getUserFromSession(context);

        Mail mail = mailProvider.get();

        // fill the mail with content:
        mail.setSubject("Techies notification");

        mail.setFrom("notification@zhenximi.me");

        mail.addReplyTo("notification@zhenximi.me");

        mail.setCharset("utf-8");
        mail.addHeader("header1", "value1");
        mail.addHeader("header2", "value2");

        mail.addTo(target.getEmail());


//        mail.addCc("cc1@domain");
//        mail.addCc("cc2@domain");
//
//        mail.addBcc("bcc1@domain");
//        mail.addBcc("bcc2@domain");

        mail.setBodyHtml("<h1>" + actualUser.getUsername() + "want to be friend with you</h1>"+ "<a href=" + EMAIL_BODY + ">LOGIN TO CHECK OUT</a>");

        mail.setBodyText("Test bodyText: Techies want to be nerds and friends.");

        // finally send the mail
        try {
            postoffice.send(mail);
        } catch (EmailException | AddressException e) {
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
