package com.example.mapdemo.Database;
import androidx.room.*;

    @Entity(tableName = "users",indices = {@Index(value = {"account"})})
    public class User {
        @PrimaryKey(autoGenerate = true)
        private int uid;

        @ColumnInfo(name = "account")
        public String account;

        @ColumnInfo(name = "password")
        public String password;
        @ColumnInfo(name = "name")
        public String name;
        @ColumnInfo(name = "slogan")
        public String slogan;
        @ColumnInfo(name = "age")
        public String age;
        @ColumnInfo(name = "place")
        public String place;
        @ColumnInfo(name = "gender")
        public String gender;
        @ColumnInfo(name = "avatar")
        public String avatar;


        public User(String account,String password){
            this.account=account;
            this.password=password;
        }

        public String getSlogan() {
            return slogan;
        }

        public void setSlogan(String slogan) {
            this.slogan = slogan;
        }

        public String getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "User{" +
                    "uid=" + uid +
                    ", account='" + account + '\'' +
                    ", password='" + password + '\'' +
                    ", name='" + name + '\'' +
                    ", slogan='" + slogan + '\'' +
                    ", age='" + age + '\'' +
                    ", place='" + place + '\'' +
                    ", gender='" + gender + '\'' +
                    ", avatar='" + avatar + '\'' +
                    '}';
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String place) {
            this.place = place;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public int getUid() {
            return uid;
        }

        public String getAccount() {
            return account;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

    }


