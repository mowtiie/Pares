package com.mowtiie.pares.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "pares.db";

    private static final String[] TABLES = {
            "users", "swipes", "matches", "messages", "posts", "post_likes",
            "comments", "follows", "blocks", "reports", "notifications", "payments"
    };

    private static final String[] MALE_NAMES = {"Miguel", "Carlo", "Jose", "Paolo", "Rafael", "Daniel", "Marco", "Luis", "Andres", "Noel", "Gabriel", "Ethan", "Jun", "Kenji", "Arman", "Diego"};
    private static final String[] FEMALE_NAMES = {"Maria", "Angela", "Sofia", "Bianca", "Camille", "Isabel", "Patricia", "Lea", "Nina", "Clara", "Hannah", "Ruth", "Elise", "Mika", "Yara", "Trisha"};
    private static final String[] LAST_NAMES = {"Santos", "Reyes", "Cruz", "Garcia", "Mendoza", "Torres", "Flores", "Ramos", "Castillo", "Bautista", "Navarro", "Aquino", "Villanueva", "Domingo", "Salazar", "Lim"};

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT, last_name TEXT, email TEXT, phone TEXT, password TEXT, role TEXT DEFAULT 'FREE', bio TEXT DEFAULT '', age INTEGER DEFAULT 18, gender TEXT DEFAULT '', location TEXT DEFAULT '', religion TEXT DEFAULT '', language TEXT DEFAULT '', hobbies TEXT DEFAULT '', avatar TEXT DEFAULT '🙂', visibility TEXT DEFAULT 'PUBLIC', status TEXT DEFAULT 'ACTIVE', premium_plan TEXT DEFAULT '', premium_until TEXT DEFAULT '', pref_gender TEXT DEFAULT 'Any', pref_age_min INTEGER DEFAULT 18, pref_age_max INTEGER DEFAULT 60, pref_location TEXT DEFAULT 'Any', pref_religion TEXT DEFAULT 'Any', pref_language TEXT DEFAULT 'Any', pref_hobbies TEXT DEFAULT '', created TEXT)");
        db.execSQL("CREATE TABLE swipes (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, target_id INTEGER, day TEXT, action TEXT)");
        db.execSQL("CREATE TABLE matches (id INTEGER PRIMARY KEY AUTOINCREMENT, user_a INTEGER, user_b INTEGER, created TEXT, status TEXT)");
        db.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY AUTOINCREMENT, sender_id INTEGER, receiver_id INTEGER, body TEXT, time TEXT, hidden INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE posts (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, body TEXT, time TEXT, hidden INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE post_likes (id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, user_id INTEGER)");
        db.execSQL("CREATE TABLE comments (id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, user_id INTEGER, body TEXT, time TEXT)");
        db.execSQL("CREATE TABLE follows (id INTEGER PRIMARY KEY AUTOINCREMENT, follower_id INTEGER, followee_id INTEGER)");
        db.execSQL("CREATE TABLE blocks (id INTEGER PRIMARY KEY AUTOINCREMENT, blocker_id INTEGER, blocked_id INTEGER)");
        db.execSQL("CREATE TABLE reports (id INTEGER PRIMARY KEY AUTOINCREMENT, reporter_id INTEGER, target_type TEXT, target_id INTEGER, reason TEXT, status TEXT, time TEXT)");
        db.execSQL("CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, type TEXT, body TEXT, time TEXT, seen INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE payments (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, plan TEXT, amount INTEGER, method TEXT, time TEXT)");
        seedDemo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String table : TABLES) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }

    private void seedDemo(SQLiteDatabase db) {
        insertUser(db, "Alex", "Rivera", "free@demo.com", "09170000001", Data.ROLE_FREE, "Coffee lover looking for someone to explore the city with.", 27, "Male", "Manila", "Catholic", "English", "Music,Travel,Hiking", "😎", Data.VISIBILITY_PUBLIC, "", "", Data.today());
        insertUser(db, "Priya", "Shah", "premium@demo.com", "09170000002", Data.ROLE_PREMIUM, "Foodie and weekend traveler. Let's swap recipes.", 26, "Female", "Cebu", "Buddhist", "English", "Cooking,Travel,Photography", "🥰", Data.VISIBILITY_PUBLIC, Data.PLAN_ANNUAL, Data.dateAfterDays(200), Data.dateAfterDays(-40));
        insertUser(db, "Admin", "Pares", "admin@demo.com", "09170000003", Data.ROLE_ADMIN, "Keeping Pares safe.", 30, "Other", "Manila", "None", "English", "Reading", "🤩", Data.VISIBILITY_PUBLIC, "", "", Data.dateAfterDays(-90));
        insertUser(db, "Sam", "Social", "social@demo.com", "09170000004", Data.ROLE_FREE, "Signed in with a social account.", 29, "Male", "Davao", "Christian", "Filipino", "Gaming,Movies,Fitness", "😄", Data.VISIBILITY_PUBLIC, "", "", Data.dateAfterDays(-5));

        String[] names = new String[32];
        for (int i = 0; i < 32; i++) {
            String gender = i % 10 == 9 ? "Other" : i % 2 == 0 ? "Male" : "Female";
            String first = gender.equals("Female") ? FEMALE_NAMES[i / 2 % 16] : MALE_NAMES[i / 2 % 16];
            String last = LAST_NAMES[(i * 5 + i / 3) % 16];
            names[i] = first;
            String hobbies = Data.HOBBIES[i % 10] + "," + Data.HOBBIES[(i + 3) % 10] + "," + Data.HOBBIES[(i + 6) % 10];
            boolean premium = i % 6 == 0;
            String plan = premium ? (i % 12 == 0 ? Data.PLAN_MONTHLY : Data.PLAN_ANNUAL) : "";
            String until = premium ? Data.dateAfterDays(i % 12 == 0 ? 30 : 365) : "";
            insertUser(db, first, last,
                    (first + "." + last + i).toLowerCase(Locale.US) + "@example.com",
                    String.format(Locale.US, "0918%07d", 1000 + i),
                    premium ? Data.ROLE_PREMIUM : Data.ROLE_FREE,
                    "Hi, I'm " + first + ". I enjoy " + Data.HOBBIES[i % 10].toLowerCase(Locale.US) + " and meeting new people.",
                    20 + (i * 7) % 16,
                    gender,
                    Data.LOCATIONS[(i + i / 2) % 6],
                    Data.RELIGIONS[(i * 3 + i / 4) % 5],
                    Data.LANGUAGES[(i + i / 3) % 5],
                    hobbies,
                    Data.AVATARS[(i * 3) % 10],
                    i % 12 == 0 ? Data.VISIBILITY_PRIVATE : Data.VISIBILITY_PUBLIC,
                    plan,
                    until,
                    Data.dateAfterDays(-(i % 7)));
        }
        seedActivity(db, names);
        seedSocial(db);
    }

    private void seedSocial(SQLiteDatabase db) {
        int[] authors = {1, 2, 6, 8, 10, 13, 18, 9};
        String[] bodies = {
                "Trail run this weekend, anyone up for a hike?",
                "Tried a new adobo recipe today. Recommendations welcome!",
                "Coffee first, everything else later.",
                "Just finished a great book. Any recommendations?",
                "Sunset photos from Baguio are unreal.",
                "Gym streak: day 30!",
                "Movie night plans, what should we watch?",
                "Learning guitar, wish me luck."
        };
        for (int p = 0; p < authors.length; p++) {
            ContentValues post = new ContentValues();
            post.put("user_id", authors[p]);
            post.put("body", bodies[p]);
            post.put("time", Data.dateAfterDays(-(p % 4)) + String.format(Locale.US, " %02d:%02d", 8 + p, 10 + p * 5));
            post.put("hidden", 0);
            db.insert("posts", null, post);
            for (int k = 0; k <= p % 4; k++) {
                ContentValues like = new ContentValues();
                like.put("post_id", p + 1);
                like.put("user_id", 5 + (p * 3 + k) % 32);
                db.insert("post_likes", null, like);
            }
        }
        addComment(db, 1, 7, "Count me in!");
        addComment(db, 1, 9, "Which trail?");
        addComment(db, 2, 1, "Try it with calamansi.");
        addComment(db, 4, 2, "Read The Alchemist, loved it.");
        addComment(db, 6, 11, "Keep it up!");

        int[][] follows = {{1, 7}, {1, 9}, {1, 13}, {6, 1}, {8, 1}, {10, 1}, {12, 1}, {2, 5}, {2, 8}, {11, 2}, {13, 2}, {1, 2}, {2, 1}};
        for (int[] pair : follows) {
            ContentValues follow = new ContentValues();
            follow.put("follower_id", pair[0]);
            follow.put("followee_id", pair[1]);
            db.insert("follows", null, follow);
        }

        addPayment(db, 2, Data.PLAN_ANNUAL, Data.PRICE_ANNUAL, "Credit card", Data.dateAfterDays(-165));
        for (int i = 0; i < 32; i += 6) {
            boolean monthly = i % 12 == 0;
            addPayment(db, 5 + i, monthly ? Data.PLAN_MONTHLY : Data.PLAN_ANNUAL, monthly ? Data.PRICE_MONTHLY : Data.PRICE_ANNUAL, monthly ? "E-wallet" : "Debit card", Data.dateAfterDays(-(i % 5)));
        }

        addReport(db, 1, Data.TARGET_PROFILE, 25, "Fake profile", Data.REPORT_OPEN);
        addReport(db, 2, Data.TARGET_POST, 3, "Spam", Data.REPORT_OPEN);
        addReport(db, 6, Data.TARGET_MESSAGE, 1, "Harassment", Data.REPORT_OPEN);
        addReport(db, 8, Data.TARGET_PROFILE, 30, "Inappropriate content", Data.REPORT_RESOLVED);
    }

    private void addPayment(SQLiteDatabase db, int userId, String plan, int amount, String method, String day) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("plan", plan);
        values.put("amount", amount);
        values.put("method", method);
        values.put("time", day + " 12:00");
        db.insert("payments", null, values);
    }

    private void addComment(SQLiteDatabase db, int postId, int userId, String body) {
        ContentValues values = new ContentValues();
        values.put("post_id", postId);
        values.put("user_id", userId);
        values.put("body", body);
        values.put("time", Data.dateAfterDays(-1) + " 21:30");
        db.insert("comments", null, values);
    }

    private void addReport(SQLiteDatabase db, int reporterId, String targetType, int targetId, String reason, String status) {
        ContentValues values = new ContentValues();
        values.put("reporter_id", reporterId);
        values.put("target_type", targetType);
        values.put("target_id", targetId);
        values.put("reason", reason);
        values.put("status", status);
        values.put("time", Data.dateAfterDays(-1) + " 22:00");
        db.insert("reports", null, values);
    }

    private void seedActivity(SQLiteDatabase db, String[] names) {
        String yesterday = Data.dateAfterDays(-1);
        for (int i : new int[]{1, 3, 5, 7, 11}) {
            addSwipe(db, 5 + i, 1, yesterday, Data.ACTION_LIKE);
        }
        for (int i : new int[]{1, 3, 5}) {
            addNotification(db, 1, Data.NOTIFY_LIKE, names[i] + " liked you", yesterday + " 20:15");
        }
        for (int i : new int[]{8, 10, 14}) {
            addSwipe(db, 5 + i, 2, yesterday, Data.ACTION_LIKE);
        }
        addNotification(db, 2, Data.NOTIFY_LIKE, names[8] + " liked you", yesterday + " 21:40");
        addNotification(db, 2, Data.NOTIFY_LIKE, names[10] + " liked you", yesterday + " 22:05");

        seedMatch(db, 1, 13, names[13], new String[]{"Hi Alex! Loved your profile.", "Thanks! Your hiking photos are great.", "We should go on a trail some weekend.", "Count me in. Any favorite spot?"});
        seedMatch(db, 1, 15, names[15], new String[]{"Hey, we matched!", "Hi! Happy to see that.", "How's your week going?"});
        seedMatch(db, 2, 2, names[2], new String[]{"Hello Priya, I saw you like cooking.", "Yes! Do you cook too?", "Only breakfast, but I'm learning."});
        seedMatch(db, 2, 4, names[4], new String[]{"Hi! Nice to match with you.", "Likewise, how are you?"});
    }

    private void seedMatch(SQLiteDatabase db, int demoId, int memberIndex, String memberName, String[] lines) {
        int memberId = 5 + memberIndex;
        String yesterday = Data.dateAfterDays(-1);
        addSwipe(db, demoId, memberId, yesterday, Data.ACTION_LIKE);
        addSwipe(db, memberId, demoId, yesterday, Data.ACTION_LIKE);
        ContentValues match = new ContentValues();
        match.put("user_a", demoId);
        match.put("user_b", memberId);
        match.put("created", yesterday);
        match.put("status", Data.MATCH_ACTIVE);
        db.insert("matches", null, match);
        addNotification(db, demoId, Data.NOTIFY_MATCH, "You matched with " + memberName, yesterday + " 18:00");
        for (int line = 0; line < lines.length; line++) {
            ContentValues message = new ContentValues();
            message.put("sender_id", line % 2 == 0 ? memberId : demoId);
            message.put("receiver_id", line % 2 == 0 ? demoId : memberId);
            message.put("body", lines[line]);
            message.put("time", yesterday + String.format(Locale.US, " 18:%02d", 5 + line * 3));
            db.insert("messages", null, message);
        }
        addNotification(db, demoId, Data.NOTIFY_MESSAGE, "New message from " + memberName, yesterday + " 19:00");
    }

    private void addSwipe(SQLiteDatabase db, int userId, int targetId, String day, String action) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("target_id", targetId);
        values.put("day", day);
        values.put("action", action);
        db.insert("swipes", null, values);
    }

    private void addNotification(SQLiteDatabase db, int userId, String type, String body, String time) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("type", type);
        values.put("body", body);
        values.put("time", time);
        values.put("seen", 0);
        db.insert("notifications", null, values);
    }

    private void insertUser(SQLiteDatabase db, String first, String last, String email, String phone, String role, String bio, int age, String gender, String location, String religion, String language, String hobbies, String avatar, String visibility, String plan, String until, String created) {
        ContentValues values = new ContentValues();
        values.put("first_name", first);
        values.put("last_name", last);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", Data.DEMO_PASSWORD);
        values.put("role", role);
        values.put("bio", bio);
        values.put("age", age);
        values.put("gender", gender);
        values.put("location", location);
        values.put("religion", religion);
        values.put("language", language);
        values.put("hobbies", hobbies);
        values.put("avatar", avatar);
        values.put("visibility", visibility);
        values.put("status", Data.STATUS_ACTIVE);
        values.put("premium_plan", plan);
        values.put("premium_until", until);
        values.put("pref_gender", gender.equals("Male") ? "Female" : gender.equals("Female") ? "Male" : Data.ANY);
        values.put("created", created);
        db.insert("users", null, values);
    }
}
