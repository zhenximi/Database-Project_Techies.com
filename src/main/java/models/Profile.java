package models;

/**
 * Created by Tom on 16/4/24.
 */

import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.*;

@Entity
public class Profile {
    private Long id;
    private UserTable author;
    private String birthday;
    private String gender;
    private String hobby;
    private String marital_status;
    private String work_place;
    //private String read_me;
    private String helper;
    //private String personal;


    public Profile(UserTable author, String birthday, String gender, String hobby, String marital_status, String work_place,  String helper){
        this.author=author;
        this.birthday=birthday;
        this.gender=gender;
        this.hobby=hobby;
        this.marital_status=marital_status;
        this.work_place=work_place;
        //this.read_me=read_me;
        this.helper=helper;
    }
    public Profile(){}

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "profile_id")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the author
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    public UserTable getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(UserTable author) {
        this.author = author;
    }


    /**
     * @return the content
     */
    public String getBirthday() {
        return birthday;
    }




    public void   setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender(){ return gender;}

    public void setGender(String gender){this.gender=gender;}

    /**
     * @return the timestamp
     */
    public String  getHobby() {
        return hobby;
    }


    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getMarital_status() {
        return marital_status;
    }

    public void setMarital_status(String marital_status) {
        this.marital_status=marital_status;
    }

    public String getWork_place(){
        return work_place;
    }
    public void setWork_place(String work_place){ this.work_place=work_place; }

//    public String getRead_me(){
//        return read_me;
//    }
//    public void setRead_me(String read_me){ this.read_me=read_me; }
//

    public String getHelper(){
        return helper;
    }


    public void setHelper(String helper) {
        this.helper = helper;
    }

}
