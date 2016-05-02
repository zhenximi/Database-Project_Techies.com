
package dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import models.Comment;
import models.Post;
import models.UserTable;
import models.DiaryComment;
import models.Diary;

public class DiaryCommentDao {
    @Inject
    Provider<EntityManager> EntityManagerProvider;

    public List<DiaryComment> getCommentsByPosts(List<Diary> diary) {
        if(diary.size() > 0) {
            EntityManager em = EntityManagerProvider.get();


            List<DiaryComment> diarycomments = new ArrayList<>();
            String strQuery = "SELECT x FROM DiaryComment x WHERE diary_id IN (";
            for (int i = 0; i < diary.size(); i++) {
                strQuery += "'" + diary.get(i).getId().toString() + "'";

                if (i != diary.size() - 1) {
                    strQuery += ", ";
                }
            }
            strQuery += ") ORDER BY timestamp DESC";

            Query q = em.createQuery(strQuery);
            diarycomments = (List<DiaryComment>) q.getResultList();

            return diarycomments;
        }
        return null;
    }
    public List<DiaryComment> getCommentsBySearchresult(Diary diary){

        EntityManager em = EntityManagerProvider.get();


        List<DiaryComment> comments = new ArrayList<DiaryComment>();
        String strQuery = "SELECT x FROM Diarycomment x WHERE diary_id IN (";

        strQuery += "'" + diary.getId().toString() + "'";



        strQuery += ") ORDER BY timestamp DESC";

        Query q = em.createQuery(strQuery);
        comments = (List<DiaryComment>) q.getResultList();

        return comments;

    }
    public List<DiaryComment> getCommentsBydiary(Diary diary) {

        EntityManager em = EntityManagerProvider.get();


        List<DiaryComment> diarycomments = new ArrayList<>();
        String strQuery = "SELECT x FROM DiaryComment x WHERE diary_id IN (";

        strQuery += "'" + diary.getId().toString() + "'";

        strQuery += ") ORDER BY timestamp DESC";

        Query q = em.createQuery(strQuery);
        diarycomments = (List<DiaryComment>) q.getResultList();


            return diarycomments;


    }
}
