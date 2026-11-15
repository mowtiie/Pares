package com.mowtiie.pares.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Data {

    public static final String ROLE_FREE = "FREE";
    public static final String ROLE_PREMIUM = "PREMIUM";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String VISIBILITY_PUBLIC = "PUBLIC";
    public static final String VISIBILITY_PRIVATE = "PRIVATE";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    public static final String ACTION_PENDING = "PENDING";
    public static final String ACTION_LIKE = "LIKE";
    public static final String ACTION_SKIP = "SKIP";

    public static final String MATCH_ACTIVE = "ACTIVE";
    public static final String MATCH_UNMATCHED = "UNMATCHED";

    public static final String TARGET_POST = "POST";
    public static final String TARGET_MESSAGE = "MESSAGE";
    public static final String TARGET_PROFILE = "PROFILE";

    public static final String REPORT_OPEN = "OPEN";
    public static final String REPORT_RESOLVED = "RESOLVED";

    public static final String NOTIFY_LIKE = "LIKE";
    public static final String NOTIFY_MESSAGE = "MESSAGE";
    public static final String NOTIFY_MATCH = "MATCH";

    public static final String PLAN_MONTHLY = "MONTHLY";
    public static final String PLAN_ANNUAL = "ANNUAL";

    public static final int DAILY_LIMIT = 10;

    public static final int LOGIN_OK = 0;
    public static final int LOGIN_INVALID = 1;
    public static final int LOGIN_SUSPENDED = 2;

    public static final int FIRST_MEMBER_ID = 5;
    public static final int LAST_MEMBER_ID = 36;

    public static final String[] CANNED_REPLIES = {
            "Hey! Great to hear from you 😊",
            "Haha, that made my day!",
            "Tell me more, I'm curious.",
            "Sounds fun, when are you free?",
            "I was just thinking about you."
    };

    public static final String ANY = "Any";
    public static final String DEMO_PASSWORD = "demo1234";
    public static final String SOCIAL_EMAIL = "social@demo.com";

    public static final String[] GENDERS = {"Male", "Female", "Other"};
    public static final String[] LOCATIONS = {"Manila", "Cebu", "Davao", "Quezon City", "Baguio", "Iloilo"};
    public static final String[] RELIGIONS = {"Catholic", "Christian", "Muslim", "Buddhist", "None"};
    public static final String[] LANGUAGES = {"English", "Filipino", "Cebuano", "Ilocano", "Hiligaynon"};
    public static final String[] HOBBIES = {"Music", "Travel", "Cooking", "Gaming", "Reading", "Fitness", "Movies", "Photography", "Hiking", "Art"};
    public static final String[] AVATARS = {"😀", "😎", "🙂", "😊", "🤗", "😄", "🥰", "😇", "🤩", "😺"};

    public static int USER_ID;
    public static String USER_FIRST_NAME = "";
    public static String USER_LAST_NAME = "";
    public static String USER_EMAIL = "";
    public static String USER_PHONE = "";
    public static String USER_ROLE = "";
    public static String USER_BIO = "";
    public static String USER_AGE = "";
    public static String USER_GENDER = "";
    public static String USER_LOCATION = "";
    public static String USER_RELIGION = "";
    public static String USER_LANGUAGE = "";
    public static String USER_HOBBIES = "";
    public static String USER_AVATAR = "";
    public static String USER_VISIBILITY = "";
    public static String USER_STATUS = "";
    public static String USER_PREMIUM_PLAN = "";
    public static String USER_PREMIUM_UNTIL = "";

    private static DbHelper helper;

    public static SQLiteDatabase db(Context context) {
        if (helper == null) {
            helper = new DbHelper(context.getApplicationContext());
        }
        return helper.getWritableDatabase();
    }

    public static String money(int amount) {
        return "USD " + amount;
    }

    public static String today() {
        return dateAfterDays(0);
    }

    public static String dateAfterDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    public static int login(Context context, String identifier, String password) {
        Cursor cursor = db(context).rawQuery(
                "SELECT id, status FROM users WHERE (lower(email) = lower(?) OR phone = ?) AND password = ?",
                new String[]{identifier, identifier, password});
        int result = LOGIN_INVALID;
        if (cursor.moveToFirst()) {
            if (STATUS_SUSPENDED.equals(cursor.getString(1))) {
                result = LOGIN_SUSPENDED;
            } else {
                loadUser(context, cursor.getInt(0));
                result = LOGIN_OK;
            }
        }
        cursor.close();
        return result;
    }

    public static void signUp(Context context, String firstName, String lastName, String email, String phone, String password) {
        ContentValues values = new ContentValues();
        values.put("first_name", firstName);
        values.put("last_name", lastName);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", password);
        values.put("role", ROLE_FREE);
        values.put("created", today());
        long id = db(context).insert("users", null, values);
        loadUser(context, (int) id);
    }

    public static boolean emailExists(Context context, String email) {
        return exists(context, "SELECT 1 FROM users WHERE lower(email) = lower(?)", email);
    }

    public static boolean phoneExists(Context context, String phone) {
        return exists(context, "SELECT 1 FROM users WHERE phone = ?", phone);
    }

    public static void updatePassword(Context context, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("password", password);
        db(context).update("users", values, "lower(email) = lower(?)", new String[]{email});
    }

    public static void loadUser(Context context, int id) {
        Cursor cursor = db(context).rawQuery(
                "SELECT id, first_name, last_name, email, phone, role, bio, age, gender, location, religion, language, hobbies, avatar, visibility, status, premium_plan, premium_until FROM users WHERE id = ?",
                new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            USER_ID = cursor.getInt(0);
            USER_FIRST_NAME = cursor.getString(1);
            USER_LAST_NAME = cursor.getString(2);
            USER_EMAIL = cursor.getString(3);
            USER_PHONE = cursor.getString(4);
            USER_ROLE = cursor.getString(5);
            USER_BIO = cursor.getString(6);
            USER_AGE = cursor.getString(7);
            USER_GENDER = cursor.getString(8);
            USER_LOCATION = cursor.getString(9);
            USER_RELIGION = cursor.getString(10);
            USER_LANGUAGE = cursor.getString(11);
            USER_HOBBIES = cursor.getString(12);
            USER_AVATAR = cursor.getString(13);
            USER_VISIBILITY = cursor.getString(14);
            USER_STATUS = cursor.getString(15);
            USER_PREMIUM_PLAN = cursor.getString(16);
            USER_PREMIUM_UNTIL = cursor.getString(17);
        }
        cursor.close();
    }

    public static void logout() {
        USER_ID = 0;
        USER_FIRST_NAME = "";
        USER_LAST_NAME = "";
        USER_EMAIL = "";
        USER_PHONE = "";
        USER_ROLE = "";
        USER_BIO = "";
        USER_AGE = "";
        USER_GENDER = "";
        USER_LOCATION = "";
        USER_RELIGION = "";
        USER_LANGUAGE = "";
        USER_HOBBIES = "";
        USER_AVATAR = "";
        USER_VISIBILITY = "";
        USER_STATUS = "";
        USER_PREMIUM_PLAN = "";
        USER_PREMIUM_UNTIL = "";
    }

    public static String me() {
        return String.valueOf(USER_ID);
    }

    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
    }

    public static ArrayList<String[]> rows(Context context, String sql, String... args) {
        Cursor cursor = db(context).rawQuery(sql, args);
        ArrayList<String[]> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            String[] row = new String[cursor.getColumnCount()];
            for (int i = 0; i < row.length; i++) {
                String text = cursor.getString(i);
                row[i] = text == null ? "" : text;
            }
            list.add(row);
        }
        cursor.close();
        return list;
    }

    public static String value(Context context, String sql, String... args) {
        ArrayList<String[]> list = rows(context, sql, args);
        return list.isEmpty() ? "" : list.get(0)[0];
    }

    public static boolean isSeededMember(String id) {
        int number = Integer.parseInt(id);
        return number >= FIRST_MEMBER_ID && number <= LAST_MEMBER_ID;
    }

    public static void saveProfile(Context context, String firstName, String lastName, String bio, int age, String gender, String location, String religion, String language, String hobbies, String avatar) {
        ContentValues values = new ContentValues();
        values.put("first_name", firstName);
        values.put("last_name", lastName);
        values.put("bio", bio);
        values.put("age", age);
        values.put("gender", gender);
        values.put("location", location);
        values.put("religion", religion);
        values.put("language", language);
        values.put("hobbies", hobbies);
        values.put("avatar", avatar);
        db(context).update("users", values, "id = ?", new String[]{me()});
        loadUser(context, USER_ID);
    }

    public static void savePreferences(Context context, String gender, int ageMin, int ageMax, String location, String religion, String language, String hobbies) {
        ContentValues values = new ContentValues();
        values.put("pref_gender", gender);
        values.put("pref_age_min", ageMin);
        values.put("pref_age_max", ageMax);
        values.put("pref_location", location);
        values.put("pref_religion", religion);
        values.put("pref_language", language);
        values.put("pref_hobbies", hobbies);
        db(context).update("users", values, "id = ?", new String[]{me()});
    }

    public static void addNotification(Context context, String userId, String type, String body) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("type", type);
        values.put("body", body);
        values.put("time", now());
        values.put("seen", 0);
        db(context).insert("notifications", null, values);
    }

    public static int unseenCount(Context context) {
        return Integer.parseInt(value(context, "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND seen = 0", me()));
    }

    public static void ensureSuggestions(Context context) {
        String me = me();
        String day = today();
        int have = Integer.parseInt(value(context, "SELECT COUNT(*) FROM swipes WHERE user_id = ? AND day = ?", me, day));
        if (have >= DAILY_LIMIT) {
            return;
        }
        String[] pref = rows(context, "SELECT pref_gender, pref_age_min, pref_age_max, pref_location, pref_religion, pref_language, pref_hobbies FROM users WHERE id = ?", me).get(0);
        ArrayList<String[]> candidates = rows(context,
                "SELECT id, gender, age, location, religion, language, hobbies, EXISTS(SELECT 1 FROM swipes x WHERE x.user_id = users.id AND x.target_id = ? AND x.action = 'LIKE') FROM users"
                        + " WHERE id <> ? AND role <> 'ADMIN' AND status = 'ACTIVE' AND visibility = 'PUBLIC'"
                        + " AND id NOT IN (SELECT target_id FROM swipes WHERE user_id = ?)"
                        + " AND id NOT IN (SELECT blocked_id FROM blocks WHERE blocker_id = ?)"
                        + " AND id NOT IN (SELECT blocker_id FROM blocks WHERE blocked_id = ?)"
                        + " AND id NOT IN (SELECT user_b FROM matches WHERE user_a = ? AND status = 'ACTIVE')"
                        + " AND id NOT IN (SELECT user_a FROM matches WHERE user_b = ? AND status = 'ACTIVE')",
                me, me, me, me, me, me, me);
        ArrayList<int[]> ranked = new ArrayList<>();
        for (String[] row : candidates) {
            int age = Integer.parseInt(row[2]);
            boolean hard = (pref[0].equals(ANY) || pref[0].equals(row[1]))
                    && age >= Integer.parseInt(pref[1]) && age <= Integer.parseInt(pref[2]);
            int score = 0;
            if (row[3].equals(pref[3])) score++;
            if (row[4].equals(pref[4])) score++;
            if (row[5].equals(pref[5])) score++;
            for (String hobby : pref[6].split(",")) {
                if (!hobby.isEmpty() && ("," + row[6] + ",").contains("," + hobby + ",")) {
                    score++;
                }
            }
            ranked.add(new int[]{Integer.parseInt(row[0]), hard ? 1 : 0, Integer.parseInt(row[7]), score});
        }
        ranked.sort((a, b) -> a[1] != b[1] ? b[1] - a[1] : a[2] != b[2] ? b[2] - a[2] : b[3] - a[3]);
        for (int i = 0; i < ranked.size() && have < DAILY_LIMIT; i++, have++) {
            ContentValues values = new ContentValues();
            values.put("user_id", me);
            values.put("target_id", ranked.get(i)[0]);
            values.put("day", day);
            values.put("action", ACTION_PENDING);
            db(context).insert("swipes", null, values);
        }
    }

    public static boolean swipe(Context context, String swipeId, String targetId, String action) {
        ContentValues values = new ContentValues();
        values.put("action", action);
        db(context).update("swipes", values, "id = ?", new String[]{swipeId});
        return ACTION_LIKE.equals(action) && afterLike(context, targetId);
    }

    public static boolean afterLike(Context context, String targetId) {
        String me = me();
        addNotification(context, targetId, NOTIFY_LIKE, USER_FIRST_NAME + " liked you");
        boolean mutual = !value(context, "SELECT 1 FROM swipes WHERE user_id = ? AND target_id = ? AND action = 'LIKE'", targetId, me).isEmpty();
        boolean alreadyMatched = !value(context, "SELECT 1 FROM matches WHERE status = 'ACTIVE' AND ((user_a = ? AND user_b = ?) OR (user_a = ? AND user_b = ?))", me, targetId, targetId, me).isEmpty();
        if (!mutual || alreadyMatched) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put("user_a", me);
        values.put("user_b", targetId);
        values.put("created", today());
        values.put("status", MATCH_ACTIVE);
        db(context).insert("matches", null, values);
        String targetName = value(context, "SELECT first_name FROM users WHERE id = ?", targetId);
        addNotification(context, me, NOTIFY_MATCH, "You matched with " + targetName);
        addNotification(context, targetId, NOTIFY_MATCH, "You matched with " + USER_FIRST_NAME);
        return true;
    }

    public static ArrayList<String[]> messages(Context context, String otherId) {
        String me = me();
        return rows(context,
                "SELECT id, sender_id, body, time FROM messages WHERE hidden = 0 AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) ORDER BY time, id",
                me, otherId, otherId, me);
    }

    public static void addMessage(Context context, String senderId, String receiverId, String body) {
        ContentValues values = new ContentValues();
        values.put("sender_id", senderId);
        values.put("receiver_id", receiverId);
        values.put("body", body);
        values.put("time", now());
        db(context).insert("messages", null, values);
        String senderName = value(context, "SELECT first_name FROM users WHERE id = ?", senderId);
        addNotification(context, receiverId, NOTIFY_MESSAGE, "New message from " + senderName);
    }

    public static String notBlocked(String column) {
        return " AND " + column + " NOT IN (SELECT blocked_id FROM blocks WHERE blocker_id = ?) AND " + column + " NOT IN (SELECT blocker_id FROM blocks WHERE blocked_id = ?)";
    }

    public static boolean isMatched(Context context, String otherId) {
        String me = me();
        return !value(context, "SELECT 1 FROM matches WHERE status = 'ACTIVE' AND ((user_a = ? AND user_b = ?) OR (user_a = ? AND user_b = ?))", me, otherId, otherId, me).isEmpty();
    }

    public static boolean isFollowing(Context context, String otherId) {
        return !value(context, "SELECT 1 FROM follows WHERE follower_id = ? AND followee_id = ?", me(), otherId).isEmpty();
    }

    public static void setFollowing(Context context, String otherId, boolean following) {
        if (following) {
            ContentValues values = new ContentValues();
            values.put("follower_id", me());
            values.put("followee_id", otherId);
            db(context).insert("follows", null, values);
        } else {
            db(context).delete("follows", "follower_id = ? AND followee_id = ?", new String[]{me(), otherId});
        }
    }

    public static void setMatchStatus(Context context, String otherId, String status) {
        String me = me();
        db(context).execSQL("UPDATE matches SET status = ? WHERE (user_a = ? AND user_b = ?) OR (user_a = ? AND user_b = ?)",
                new Object[]{status, me, otherId, otherId, me});
    }

    public static void block(Context context, String otherId) {
        ContentValues values = new ContentValues();
        values.put("blocker_id", me());
        values.put("blocked_id", otherId);
        db(context).insert("blocks", null, values);
        setMatchStatus(context, otherId, MATCH_UNMATCHED);
    }

    public static void unblock(Context context, String otherId) {
        db(context).delete("blocks", "blocker_id = ? AND blocked_id = ?", new String[]{me(), otherId});
    }

    public static String addReport(Context context, String targetType, String targetId, String reason) {
        ContentValues values = new ContentValues();
        values.put("reporter_id", me());
        values.put("target_type", targetType);
        values.put("target_id", targetId);
        values.put("reason", reason);
        values.put("status", REPORT_OPEN);
        values.put("time", now());
        return String.valueOf(db(context).insert("reports", null, values));
    }

    public static void deleteRow(Context context, String table, String id) {
        db(context).delete(table, "id = ?", new String[]{id});
    }

    public static String cannedReply() {
        return CANNED_REPLIES[new Random().nextInt(CANNED_REPLIES.length)];
    }

    private static boolean exists(Context context, String sql, String argument) {
        Cursor cursor = db(context).rawQuery(sql, new String[]{argument});
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }
}
