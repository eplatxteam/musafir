package com.example.musafir;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "musafir.db";
    private static final int DATABASE_VERSION = 16;

    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_BOOKINGS = "bookings";
    private static final String TABLE_COUNTRY = "country";
    private static final String TABLE_CITIES = "cities";

    // أعمدة المدن
    private static final String COL_ID = "city_id";
    private static final String COL_NAME_AR = "city_name_ar";
    private static final String city_code = "city_code";
    private static final String COL_NAME_EN = "city_name_en";
    private static final String COL_COUNTRY_ID = "country_id";
    private static final String TABLE_TYPE_TRAVELER_REQUESTS = "type_traveler_requests";
    private static final String SERVICE_HOME = "service_home";
    private static final String COL_TYPE_ID = "type_tr_id";
    private static final String COL_TYPE_NAME = "type_tr_name";
    private static final String COL_TYPE_ICON = "type_icon";
    private static final String COL_order_type = "order_type";
    private static final String TABLE_TRAVELER = "traveler_requests";
    private static final String TABLE_ROUTES = "routes_table";
    private static final String COL_INACTIVE = "inactive";

    private static final String CREATE_ROUTES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ROUTES + " (" +
                    "route_id INTEGER PRIMARY KEY, " +
                    "route_name TEXT, " +
                    "car_code TEXT, " +
                    "route_city TEXT" +
                    ")";
    private static final String TABLE_COMPANY = "company";
    private static final String COL_COMPANY_ID = "company_id";
    private static final String COL_COMPANY_NAME = "company_name";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TABLE_VEHICLE_TYPES = "vehicle_types";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES + " (message_id INTEGER PRIMARY KEY, messages TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CITIES + " (" +
                COL_ID + " INTEGER PRIMARY KEY," +
                COL_NAME_AR + " TEXT," +
                city_code + " TEXT," +
                COL_COUNTRY_ID + " INTEGER," +
                COL_NAME_EN + " TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_VEHICLE_TYPES + " (" +
                "id_vehicle_type INTEGER PRIMARY KEY, " +
                "vehicles_type TEXT NOT NULL," +
                "inactive INTEGER);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BOOKINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "trip_id INTEGER," +
                "booking_id INTEGER," +
                "passenger_id INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COUNTRY + " (" +
                "country_id INTEGER PRIMARY KEY," +
                "country_name TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TYPE_TRAVELER_REQUESTS + " (" +
                COL_TYPE_ID + " INTEGER PRIMARY KEY," +
                COL_TYPE_NAME + " TEXT," +
                COL_INACTIVE + " INTEGER," +
                COL_order_type + " INTEGER," +
                "app_page INTEGER," + // تم إضافته هنا للمستخدم الجديد
                COL_TYPE_ICON + " TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SERVICE_HOME + " (" +
                "service_id INTEGER PRIMARY KEY," +
                "service_name TEXT," +
                "inactive INTEGER," +
                "order_type INTEGER," +
                "app_page INTEGER," +
                "type_icon TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TRAVELER + "(" +
                "tr_id INTEGER PRIMARY KEY," +
                "type_tr_name TEXT," +
                "tr_status TEXT," +
                "number_passenger TEXT," +
                "type_icon TEXT," +
                "name_passenger1 TEXT," +
                "name_passenger2 TEXT," +
                "name_passenger3 TEXT," +
                "name_passenger4 TEXT," +
                "name_passenger5 TEXT," +
                "name_passenger6 TEXT," +
                "notes TEXT," +
                "country TEXT," +
                "updated_at TEXT," +          // تم إضافته هنا للمستخدم الجديد
                "number_status INTEGER DEFAULT 1," + // تم إضافته هنا للمستخدم الجديد
                "created_at TEXT)");
        db.execSQL("CREATE TABLE chat_messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "message TEXT," +
                "is_me INTEGER," +
                "time TEXT)");
        db.execSQL("CREATE TABLE cash_bank (" +
                "cb_id INTEGER PRIMARY KEY, " +
                "is_wallet_flg INTEGER, " +
                "is_active INTEGER DEFAULT 1, " +
                "comfirm_wallet_pay_flag INTEGER DEFAULT 0, " +
                "cb_name TEXT, " +
                "wallet_icon TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS code_details (" +
                "type_no INTEGER, " +
                "code_no INTEGER, " +
                "code_l_nm TEXT, " +
                "show_in_app INTEGER DEFAULT 1, " +
                "code_icon TEXT, " +
                "PRIMARY KEY(type_no, code_no))");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COMPANY + " (" +
                COL_COMPANY_ID + " INTEGER PRIMARY KEY, " +
                "is_active INTEGER ," +
                COL_COMPANY_NAME + " TEXT)");
        db.execSQL(CREATE_ROUTES_TABLE);
    }


    public List<Map<String, Object>> getAllCashBank(int is_wallet_flg) {
        List<Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cash_bank WHERE is_active = 1 and is_wallet_flg = ?", new String[]{String.valueOf(is_wallet_flg)});
        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> item = new HashMap<>();
                item.put("cb_id", cursor.getInt(cursor.getColumnIndexOrThrow("cb_id")));
                item.put("cb_name", cursor.getString(cursor.getColumnIndexOrThrow("cb_name")));
                item.put("wallet_icon", cursor.getString(cursor.getColumnIndexOrThrow("wallet_icon")));
                item.put("is_wallet_flg", cursor.getString(cursor.getColumnIndexOrThrow("is_wallet_flg")));
                item.put("comfirm_wallet_pay_flag", cursor.getInt(cursor.getColumnIndexOrThrow("comfirm_wallet_pay_flag")));
                item.put("is_active", cursor.getInt(cursor.getColumnIndexOrThrow("is_active")));

                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void saveCashBank(int id, String name, String icon, int is_wallet_flg, int comfirm_wallet_pay_flag, int is_active) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cb_id", id);
        values.put("cb_name", name);
        values.put("wallet_icon", icon);
        values.put("is_wallet_flg", is_wallet_flg);
        values.put("comfirm_wallet_pay_flag", comfirm_wallet_pay_flag);
        values.put("is_active", is_active);
        db.insertWithOnConflict("cash_bank", null, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            addColumnIfNotExists(db, TABLE_TRAVELER, "number_status", "INTEGER DEFAULT 1");
            addColumnIfNotExists(db, TABLE_TRAVELER, "updated_at", "TEXT");
        }

        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + SERVICE_HOME + " (" +
                    "service_id INTEGER PRIMARY KEY," +
                    "service_name TEXT," +
                    "inactive INTEGER," +
                    "order_type INTEGER," +
                    "app_page INTEGER," +
                    "type_icon TEXT)");

            addColumnIfNotExists(db, TABLE_TYPE_TRAVELER_REQUESTS, "app_page", "INTEGER");
        }
        if (oldVersion < 7) {

            db.execSQL("CREATE TABLE chat_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "message TEXT," +
                    "is_me INTEGER," +
                    "time TEXT)");
        }
        if (oldVersion < 8) {
            db.execSQL("CREATE TABLE cash_bank (" +
                    "cb_id INTEGER PRIMARY KEY, " +
                    "cb_name TEXT, " +
                    "wallet_icon TEXT)");
        }
        if (oldVersion < 9) {
            addColumnIfNotExists(db, "cash_bank", "is_wallet_flg", "INTEGER");
        }

        if (oldVersion < 10) {
            db.execSQL("CREATE TABLE IF NOT EXISTS code_details (" +
                    "type_no INTEGER, " +
                    "code_no INTEGER, " +
                    "code_l_nm TEXT, " +
                    "code_icon TEXT, " +
                    "PRIMARY KEY(type_no, code_no))");
        }
        if (oldVersion < 11) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COMPANY + " (" +
                    COL_COMPANY_ID + " INTEGER PRIMARY KEY, " +
                    "is_active INTEGER ," +
                    COL_COMPANY_NAME + " TEXT)");
        }
        if (oldVersion < 13) {
            addColumnIfNotExists(db, "cash_bank", "comfirm_wallet_pay_flag", "INTEGER DEFAULT 0");

        }
        if (oldVersion < 14) {
            addColumnIfNotExists(db, "cash_bank", "is_active", "INTEGER DEFAULT 1");

        }
        if (oldVersion < 16) {
            addColumnIfNotExists(db, "code_details", "show_in_app", "INTEGER DEFAULT 1");
        }

    }

    public void saveCompaniesFromJson(JSONArray jsonArray) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                ContentValues values = new ContentValues();

                values.put(COL_COMPANY_ID, obj.optInt("company_no"));
                values.put(COL_COMPANY_NAME, obj.optString("company_name"));
                values.put("is_active", obj.optString("is_active"));

                db.insertWithOnConflict(TABLE_COMPANY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAllCompanies() {
        List<Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM company WHERE is_active = 1", null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> item = new HashMap<>();
                item.put("company_no", cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPANY_ID)));
                item.put("company_name", cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPANY_NAME)));
                item.put("is_active", cursor.getString(cursor.getColumnIndexOrThrow("is_active")));
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Map<String, Object>> getCodeDetailsByType(int typeNo) {
        List<Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM code_details WHERE show_in_app = 1 and type_no = ?", new String[]{String.valueOf(typeNo)});

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> item = new HashMap<>();
                item.put("code_no", cursor.getInt(cursor.getColumnIndexOrThrow("code_no")));
                item.put("code_l_nm", cursor.getString(cursor.getColumnIndexOrThrow("code_l_nm")));
                item.put("code_icon", cursor.getString(cursor.getColumnIndexOrThrow("code_icon")));
                item.put("show_in_app", cursor.getInt(cursor.getColumnIndexOrThrow("show_in_app")));
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void saveCodeDetails(int typeNo, int codeNo, String name, String icon, int show_in_app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("type_no", typeNo);
        values.put("code_no", codeNo);
        values.put("code_l_nm", name);
        values.put("code_icon", icon);
        values.put("show_in_app", show_in_app);
        db.insertWithOnConflict("code_details", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void insertMessage(int userId, String text, boolean isMe, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("message", text);
        values.put("is_me", isMe ? 1 : 0);
        values.put("time", time);
        db.insert("chat_messages", null, values);
        db.close();

    }

    public List<Map<String, Object>> getAllMessages(int userId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM chat_messages WHERE user_id = ? ORDER BY id ASC",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> msg = new HashMap<>();
                msg.put("text", cursor.getString(cursor.getColumnIndexOrThrow("message")));
                msg.put("isMe", cursor.getInt(cursor.getColumnIndexOrThrow("is_me")) == 1);
                msg.put("time", cursor.getString(cursor.getColumnIndexOrThrow("time")));
                messages.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//        if (oldVersion < 2) {
//            addColumnIfNotExists(db, TABLE_TRAVELER, "number_status", "INTEGER DEFAULT 0");
//            addColumnIfNotExists(db, TABLE_TRAVELER, "updated_at", "TEXT");
//        }
//
//        if (oldVersion < 4) {
//            db.execSQL("CREATE TABLE IF NOT EXISTS " + SERVICE_HOME + " (" +
//                    "service_id INTEGER PRIMARY KEY," +
//                    "service_name TEXT," +
//                    "inactive INTEGER," +
//                    "order_type INTEGER," +
//                    "app_page INTEGER," +
//                    "type_icon TEXT)");
//
//            addColumnIfNotExists(db, TABLE_TYPE_TRAVELER_REQUESTS, "app_page", "INTEGER");
//        }
//    }

    private void addColumnIfNotExists(SQLiteDatabase db, String tableName, String columnName, String columnType) {
        try {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        } catch (Exception e) {
        }
    }

    public List<TypeTravelerRequest> getAllTypeTravelerRequests() {
        List<TypeTravelerRequest> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TYPE_TRAVELER_REQUESTS , null);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TYPE_TRAVELER_REQUESTS + " order by order_type", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TYPE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE_NAME));
                String icon = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE_ICON));
                int inactive = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INACTIVE));
                int app_Page = cursor.getInt(cursor.getColumnIndexOrThrow("app_page"));
                list.add(new TypeTravelerRequest(id, name, icon, inactive, app_Page));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public void insertOrUpdateTypeTravelerRequest(int typeId, String name, String icon, int inactive, int order_type, int app_Page) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TYPE_ID, typeId);
        values.put(COL_TYPE_NAME, name);
        values.put(COL_TYPE_ICON, icon);
        values.put(COL_INACTIVE, inactive);
        values.put(COL_order_type, order_type);
        values.put("app_page", app_Page);
        db.insertWithOnConflict(TABLE_TYPE_TRAVELER_REQUESTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<ServiceHome> getAllServiceHome() {
        List<ServiceHome> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SERVICE_HOME + " order by order_type", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("service_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("service_name"));
                String icon = cursor.getString(cursor.getColumnIndexOrThrow("type_icon"));
                int inactive = cursor.getInt(cursor.getColumnIndexOrThrow("inactive"));
                int app_Page = cursor.getInt(cursor.getColumnIndexOrThrow("app_page"));
                list.add(new ServiceHome(id, name, icon, inactive, app_Page));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public void insertOrUpdateServiceHome(int typeId, String name, String icon, int inactive, int order_type, int app_Page) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("service_id", typeId);
        values.put("service_name", name);
        values.put("type_icon", icon);
        values.put("inactive", inactive);
        values.put("order_type", order_type);
        values.put("app_page", app_Page);
        db.insertWithOnConflict(SERVICE_HOME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void saveRoutes(JSONArray routes) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            db.delete("routes_table", null, null);

            for (int i = 0; i < routes.length(); i++) {
                JSONObject route = routes.getJSONObject(i);

                ContentValues values = new ContentValues();
                values.put("route_id", route.getInt("route_id"));
                values.put("route_name", route.getString("route_name"));
                values.put("car_code", route.optString("car_codes_id", ""));
                values.put("route_city", route.optString("route_city", "")); // ⬅ إضافة هذا

                db.insert("routes_table", null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }


    public List<RouteModel> getAllRoutes() {
        List<RouteModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM routes_table", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("route_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("route_name"));
                String carCode = cursor.getString(cursor.getColumnIndexOrThrow("car_code"));
                String routeCity = cursor.getString(cursor.getColumnIndexOrThrow("route_city"));

                list.add(new RouteModel(id, name, carCode, routeCity));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public List<RouteModel> searchRoutesByCities(int startCity, int endCity) {
        List<RouteModel> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT * FROM routes_table " +
                        "WHERE route_city LIKE ? " +
                        "AND route_city LIKE ?";

        String startPattern = startCity + ", %";
        String endPattern = "%, " + endCity + "%";

        Cursor cursor = db.rawQuery(query, new String[]{startPattern, endPattern});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("route_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("route_name"));
                String carCode = cursor.getString(cursor.getColumnIndexOrThrow("car_code"));
                String routeCity = cursor.getString(cursor.getColumnIndexOrThrow("route_city"));

                results.add(new RouteModel(id, name, carCode, routeCity));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return results;
    }

    public class RouteModel {
        public int id;
        public String name;
        public String carCode;
        public String routeCity;

        public RouteModel(int id, String name, String carCode, String routeCity) {
            this.id = id;
            this.name = name;
            this.carCode = carCode;
            this.routeCity = routeCity;
        }
    }

    public void insertVehicleType(int id, String name, int inactive) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id_vehicle_type", id);
        values.put("vehicles_type", name);
        values.put("inactive", inactive);

        db.insert(TABLE_VEHICLE_TYPES, null, values);
    }

    public List<VehicleType> getVehicleTypes(Integer inactive_vh) {
        List<VehicleType> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;
        if (inactive_vh == null) {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_VEHICLE_TYPES, null);
        } else {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_VEHICLE_TYPES + " WHERE inactive = ?", new String[]{String.valueOf(inactive_vh)});
        }
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_vehicle_type"));
                int inactive = cursor.getInt(cursor.getColumnIndexOrThrow("inactive"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("vehicles_type"));

                list.add(new VehicleType(id, name, inactive));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public void clearVehicleTypes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VEHICLE_TYPES, null, null);
    }

    public static class VehicleType {
        int id;
        int inactive;
        String name;

        public VehicleType(int id, String name, int inactive) {
            this.id = id;
            this.inactive = inactive;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private String getLocalUpdatedAt(int trId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT updated_at FROM " + TABLE_TRAVELER + " WHERE tr_id = ?",
                new String[]{String.valueOf(trId)}
        );

        String localUpdatedAt = null;
        if (c.moveToFirst()) {
            localUpdatedAt = c.getString(0);
        }
        c.close();
        return localUpdatedAt;
    }

    public void insertOrUpdate(JSONObject item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        try {
            int tr_id = item.getInt("tr_id");
            String serverUpdatedAt = item.optString("updated_at", null);
            String localUpdatedAt = getLocalUpdatedAt(tr_id);

            // لو موجود محليًا وأحدث → لا نحدّث
            if (localUpdatedAt != null && serverUpdatedAt != null) {
                if (localUpdatedAt.compareTo(serverUpdatedAt) >= 0) {
                    return;
                }
            }

            cv.put("tr_id", tr_id);
            cv.put("type_tr_name", item.getString("type_tr_name"));
            cv.put("tr_status", item.getString("tr_status"));
            cv.put("number_passenger", item.getString("number_passenger"));
            cv.put("type_icon", item.getString("type_icon"));
            cv.put("notes", item.getString("notes"));
            cv.put("country", item.getString("country"));
            cv.put("created_at", item.getString("created_at"));
            cv.put("updated_at", serverUpdatedAt);

            cv.put("name_passenger1", item.optString("name_passenger1", ""));
            cv.put("name_passenger2", item.optString("name_passenger2", ""));
            cv.put("name_passenger3", item.optString("name_passenger3", ""));
            cv.put("name_passenger4", item.optString("name_passenger4", ""));
            cv.put("name_passenger5", item.optString("name_passenger5", ""));
            cv.put("name_passenger6", item.optString("name_passenger6", ""));
            cv.put("number_status", item.optInt("number_status", 0));

            db.insertWithOnConflict(
                    TABLE_TRAVELER,
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
            );

            // الاحتفاظ بآخر 5 فقط
            db.execSQL(
                    "DELETE FROM " + TABLE_TRAVELER +
                            " WHERE tr_id NOT IN (" +
                            " SELECT tr_id FROM " + TABLE_TRAVELER +
                            " ORDER BY created_at DESC LIMIT 5)"
            );

        } catch (JSONException e) {
        }
    }


    public List<JSONObject> getLatestRequests() {
        List<JSONObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRAVELER + " ORDER BY created_at DESC LIMIT 5", null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("tr_id", cursor.getInt(cursor.getColumnIndexOrThrow("tr_id")));
                    obj.put("type_tr_name", cursor.getString(cursor.getColumnIndexOrThrow("type_tr_name")));
                    obj.put("tr_status", cursor.getString(cursor.getColumnIndexOrThrow("tr_status")));
                    obj.put("number_passenger", cursor.getString(cursor.getColumnIndexOrThrow("number_passenger")));
                    obj.put("type_icon", cursor.getString(cursor.getColumnIndexOrThrow("type_icon")));
                    obj.put("name_passenger1", cursor.getString(cursor.getColumnIndexOrThrow("name_passenger1")));
                    obj.put("name_passenger2", cursor.getString(cursor.getColumnIndexOrThrow("name_passenger2")));
                    obj.put("name_passenger3", cursor.getString(cursor.getColumnIndexOrThrow("name_passenger3")));
                    obj.put("name_passenger4", cursor.getString(cursor.getColumnIndexOrThrow("name_passenger4")));
                    obj.put("name_passenger5", cursor.getString(cursor.getColumnIndexOrThrow("name_passenger5")));
                    obj.put("name_passenger6", cursor.getString(cursor.getColumnIndexOrThrow("name_passenger6")));
                    obj.put("notes", cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                    obj.put("country", cursor.getString(cursor.getColumnIndexOrThrow("country")));
                    obj.put("number_status", cursor.getInt(cursor.getColumnIndexOrThrow("number_status")));
                    obj.put("created_at", cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                    list.add(obj);
                } catch (JSONException e) {
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    public int getLastTrId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT tr_id FROM traveler_requests ORDER BY tr_id DESC LIMIT 1", null);
        int lastId = 0;
        if (cursor.moveToFirst()) {
            lastId = cursor.getInt(cursor.getColumnIndexOrThrow("tr_id"));
        }
        cursor.close();
        return lastId;
    }

    public void addCountry(int countryId, String countryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("country_id", countryId);
        cv.put("country_name", countryName);
        db.insertWithOnConflict(TABLE_COUNTRY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void insertOrUpdateCity(int id, String nameAr, String nameEn, int countryId, String cityCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, id);
        values.put(COL_NAME_AR, nameAr);
        values.put(city_code, cityCode);
        values.put(COL_NAME_EN, nameEn.toLowerCase());
        values.put(COL_COUNTRY_ID, countryId);
        db.insertWithOnConflict(TABLE_CITIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<City> getAllCities() {
        List<City> cities = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT city_id, city_name_ar, country_id FROM " + TABLE_CITIES, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("city_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("city_name_ar"));
                int countryId = cursor.getInt(cursor.getColumnIndexOrThrow("country_id"));
                cities.add(new City(id, name, countryId));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return cities;
    }

    public List<City> getCitiesByCountry(int countryId) {
        List<City> cities = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT city_id, city_name_ar, country_id FROM " + TABLE_CITIES + " WHERE country_id=?",
                new String[]{String.valueOf(countryId)}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("city_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("city_name_ar"));
                int cId = cursor.getInt(cursor.getColumnIndexOrThrow("country_id"));
                cities.add(new City(id, name, cId));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return cities;
    }

    public String getCityNameById(int cityId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String cityName = null;
        Cursor cursor = db.rawQuery("SELECT city_name_ar FROM " + TABLE_CITIES + " WHERE city_id=?", new String[]{String.valueOf(cityId)});
        if (cursor.moveToFirst()) {
            cityName = cursor.getString(cursor.getColumnIndexOrThrow("city_name_ar"));
        }
        cursor.close();
        db.close();
        return cityName != null ? cityName : String.valueOf(cityId);
    }


    public List<Country> getAllCountries() {
        List<Country> countries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT country_id, country_name FROM " + TABLE_COUNTRY, null);

        if (cursor.moveToFirst()) {
            do {
                countries.add(new Country(
                        cursor.getInt(cursor.getColumnIndexOrThrow("country_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("country_name"))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return countries;
    }

    public void insertMessage(int messageId, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("message_id", messageId);
        values.put("messages", message);
        db.insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }


    public boolean isBooked(int tripId, int passengerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM bookings WHERE trip_id=? AND passenger_id=?",
                new String[]{String.valueOf(tripId), String.valueOf(passengerId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public void addBooking(int tripId, int bookingId, int passengerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("trip_id", tripId);
        cv.put("booking_id", bookingId);
        cv.put("passenger_id", passengerId);
        db.insert("bookings", null, cv);
        db.close();
    }

    public int getBookingId(int tripId, int passengerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT booking_id FROM bookings WHERE trip_id=? AND passenger_id=?",
                new String[]{String.valueOf(tripId), String.valueOf(passengerId)});
        int bookingId = -1;
        if (cursor.moveToFirst()) {
            bookingId = cursor.getInt(cursor.getColumnIndexOrThrow("booking_id"));
        }
        cursor.close();
        db.close();
        return bookingId;
    }

    public void deleteBooking(int bookingId, int passengerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("bookings", "booking_id=? AND passenger_id=?", new String[]{
                String.valueOf(bookingId), String.valueOf(passengerId)
        });
        db.close();
    }

    public List<City> getAllCities2() {
        List<City> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT city_id, city_name_ar,country_id FROM cities", null);
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String name = cursor.getString(1);
                    int country_id = cursor.getInt(2);
                    list.add(new City(id, name, country_id));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return list;
    }


    public City getCityByName(String cityName) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        City city = null;

        try {
            db = this.getReadableDatabase();
            String cleanCityName = cityName.replaceAll("[\\u200E\\u200F]", "").trim();
            cursor = db.query("cities",
                    new String[]{"city_id", "city_name_ar", "country_id"},
                    "city_name_ar LIKE ?",
                    new String[]{cleanCityName},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("city_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("city_name_ar"));
                int countryId = cursor.getInt(cursor.getColumnIndexOrThrow("country_id"));
                city = new City(id, name, countryId);
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return city;
    }


    public static class City {
        public int id;
        public String name;
        public int country_id;

        public City(int id, String name, int country_id) {
            this.id = id;
            this.name = name;
            this.country_id = country_id;
        }

        public int getId() {
            return id;
        }

        public int getCountryId() {
            return country_id;
        }

        public String getNameAr() {
            return name;
        }
    }

    public static class Country {
        public int id;
        public String name;

        public Country(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class TypeTravelerRequest {
        public int type_tr_id;
        public int inactive;
        public int app_Page;
        public String type_tr_name;
        public String type_icon;

        public TypeTravelerRequest(int type_tr_id, String type_tr_name, String type_icon, int inactive, int app_Page) {
            this.type_tr_id = type_tr_id;
            this.type_tr_name = type_tr_name;
            this.type_icon = type_icon;
            this.inactive = inactive;
            this.app_Page = app_Page;
        }
    }

    public static class CashBank {
        public int cb_id;
        public int is_wallet_flg;
        public String cb_name;
        public String wallet_icon;

        public CashBank(int cb_id, String cb_name, String wallet_icon, int is_wallet_flg) {
            this.cb_id = cb_id;
            this.cb_name = cb_name;
            this.wallet_icon = wallet_icon;
            this.is_wallet_flg = is_wallet_flg;
        }
    }

    public static class ServiceHome {
        public int type_tr_id;
        public int inactive;
        public int app_Page;
        public String type_tr_name;
        public String type_icon;

        public ServiceHome(int type_tr_id, String type_tr_name, String type_icon, int inactive, int app_Page) {
            this.type_tr_id = type_tr_id;
            this.type_tr_name = type_tr_name;
            this.type_icon = type_icon;
            this.inactive = inactive;
            this.app_Page = app_Page;
        }
    }

}
