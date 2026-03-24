package com.medilink.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.medilink.app.models.Appointment;
import com.medilink.app.models.Medication;
import com.medilink.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MediLink.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String TABLE_MEDICATIONS = "medications";

    // Common columns
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";

    // User table columns
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_DOB = "dob";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_EMERGENCY_NAME = "emergency_name";
    private static final String KEY_EMERGENCY_PHONE = "emergency_phone";
    private static final String KEY_PROFILE_PIC = "profile_pic";

    // Appointment table columns
    private static final String KEY_DOCTOR_NAME = "doctor_name";
    private static final String KEY_SPECIALTY = "specialty";
    private static final String KEY_CLINIC = "clinic";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";
    private static final String KEY_STATUS = "status";
    private static final String KEY_COLOR = "color";

    // Medication table columns
    private static final String KEY_MED_NAME = "med_name";
    private static final String KEY_DOSAGE = "dosage";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_IS_TAKEN = "is_taken";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT,"
                + KEY_PHONE + " TEXT,"
                + KEY_ROLE + " TEXT,"
                + KEY_DOB + " TEXT,"
                + KEY_GENDER + " TEXT,"
                + KEY_EMERGENCY_NAME + " TEXT,"
                + KEY_EMERGENCY_PHONE + " TEXT,"
                + KEY_PROFILE_PIC + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create Appointments Table
        String CREATE_APPOINTMENTS_TABLE = "CREATE TABLE " + TABLE_APPOINTMENTS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_USER_ID + " TEXT,"
                + KEY_DOCTOR_NAME + " TEXT,"
                + KEY_SPECIALTY + " TEXT,"
                + KEY_CLINIC + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_TIME + " TEXT,"
                + KEY_STATUS + " TEXT,"
                + KEY_COLOR + " TEXT" + ")";
        db.execSQL(CREATE_APPOINTMENTS_TABLE);

        // Create Medications Table
        String CREATE_MEDICATIONS_TABLE = "CREATE TABLE " + TABLE_MEDICATIONS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_USER_ID + " TEXT,"
                + KEY_MED_NAME + " TEXT,"
                + KEY_DOSAGE + " TEXT,"
                + KEY_TIME + " TEXT,"
                + KEY_FREQUENCY + " TEXT,"
                + KEY_NOTES + " TEXT,"
                + KEY_IS_TAKEN + " INTEGER" + ")";
        db.execSQL(CREATE_MEDICATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        onCreate(db);
    }

    // --- User Operations ---

    public void syncUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getId());
        values.put(KEY_NAME, user.getName());
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_PHONE, user.getPhone());
        values.put(KEY_ROLE, user.getRole());
        values.put(KEY_DOB, user.getDob());
        values.put(KEY_GENDER, user.getGender());
        values.put(KEY_EMERGENCY_NAME, user.getEmergencyName());
        values.put(KEY_EMERGENCY_PHONE, user.getEmergencyPhone());
        values.put(KEY_PROFILE_PIC, user.getProfilePic());

        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public User getUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, KEY_ID + "=?", new String[]{userId}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            return extractUserFromCursor(cursor);
        }
        return null;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, KEY_EMAIL + "=?", new String[]{email}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            return extractUserFromCursor(cursor);
        }
        return null;
    }

    private User extractUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLE)));
        user.setDob(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DOB)));
        user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GENDER)));
        user.setEmergencyName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMERGENCY_NAME)));
        user.setEmergencyPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMERGENCY_PHONE)));
        user.setProfilePic(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_PIC)));
        cursor.close();
        return user;
    }

    // --- Appointment Operations ---

    public void syncAppointments(List<Appointment> appointments) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Appointment appt : appointments) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, appt.getId());
                values.put(KEY_USER_ID, appt.getUserId());
                values.put(KEY_DOCTOR_NAME, appt.getDoctorName());
                values.put(KEY_SPECIALTY, appt.getSpecialty());
                values.put(KEY_CLINIC, appt.getClinic());
                values.put(KEY_DATE, appt.getDate());
                values.put(KEY_TIME, appt.getTime());
                values.put(KEY_STATUS, appt.getStatus());
                values.put(KEY_COLOR, appt.getColor());
                db.insertWithOnConflict(TABLE_APPOINTMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<Appointment> getAppointments(String userId) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENTS, null, KEY_USER_ID + "=?", new String[]{userId}, null, null, KEY_DATE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Appointment appt = new Appointment(
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DOCTOR_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_SPECIALTY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLINIC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_COLOR))
                );
                list.add(appt);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // --- Medication Operations ---

    public void syncMedications(List<Medication> medications) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Medication med : medications) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, med.getId());
                values.put(KEY_USER_ID, med.getUserId());
                values.put(KEY_MED_NAME, med.getName());
                values.put(KEY_DOSAGE, med.getDosage());
                values.put(KEY_TIME, med.getTime());
                values.put(KEY_FREQUENCY, med.getFrequency());
                values.put(KEY_NOTES, med.getNotes());
                values.put(KEY_IS_TAKEN, med.isTaken() ? 1 : 0);
                db.insertWithOnConflict(TABLE_MEDICATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void syncSingleAppointment(Appointment appt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, appt.getId());
        values.put(KEY_USER_ID, appt.getUserId());
        values.put(KEY_DOCTOR_NAME, appt.getDoctorName());
        values.put(KEY_SPECIALTY, appt.getSpecialty());
        values.put(KEY_CLINIC, appt.getClinic());
        values.put(KEY_DATE, appt.getDate());
        values.put(KEY_TIME, appt.getTime());
        values.put(KEY_STATUS, appt.getStatus());
        values.put(KEY_COLOR, appt.getColor());
        db.insertWithOnConflict(TABLE_APPOINTMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void syncSingleMedication(Medication med) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, med.getId());
        values.put(KEY_USER_ID, med.getUserId());
        values.put(KEY_MED_NAME, med.getName());
        values.put(KEY_DOSAGE, med.getDosage());
        values.put(KEY_TIME, med.getTime());
        values.put(KEY_FREQUENCY, med.getFrequency());
        values.put(KEY_NOTES, med.getNotes());
        values.put(KEY_IS_TAKEN, med.isTaken() ? 1 : 0);
        db.insertWithOnConflict(TABLE_MEDICATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<Medication> getMedications(String userId) {
        List<Medication> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEDICATIONS, null, KEY_USER_ID + "=?", new String[]{userId}, null, null, KEY_TIME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Medication med = new Medication(
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_MED_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DOSAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FREQUENCY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTES))
                );
                med.setTaken(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_TAKEN)) == 1);
                list.add(med);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
