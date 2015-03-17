package com.samsonan.android.percussionstudio.providers;

import com.samsonan.android.percussionstudio.entities.InstrumentFactory;
import com.samsonan.android.percussionstudio.entities.InstrumentFactory.Instruments;
import com.samsonan.android.percussionstudio.entities.RhythmInfo;
import com.samsonan.android.percussionstudio.entities.SoundInfo;
import com.samsonan.android.percussionstudio.entities.TrackInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Helper class and methods to work with app database
 */
public class PercussionDatabase {

	private static final String TAG = "PercussionDatabase";

	// Handle to a new DatabaseHelper.
	private MetaphorsDBHelper mOpenHelper;

	public static final class RhythmTable implements BaseColumns {
        
		private RhythmTable() {
        }

        public static final String TABLE_NAME = "rhythms";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BPM = "bpm";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_BIT_LENGTH = "bit_length" ; // 1/x, x - bit length
        public static final String COLUMN_NAME_BAR_BITS = "bar_bits" ;
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_INTERNAL_FLAG= "internal";
        public static final String COLUMN_NAME_HIDDEN_FLAG= "hidden";
	}

	public static final class TrackTable implements BaseColumns {
        
		private TrackTable() {
        }

        public static final String TABLE_NAME = "tracks";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_RHYTHM_ID = "track_rhythm_id";
        public static final String COLUMN_NAME_BAR_CNT = "bar_cnt" ;
        public static final String COLUMN_NAME_CONNECTED = "connect_flag" ;
        public static final String COLUMN_NAME_INSTRUMENT = "instrument";
	}
	
	
    public static final class SoundTable implements BaseColumns {
        private SoundTable() {
        }

        public static final String TABLE_NAME = "sounds";

        public static final String COLUMN_NAME_TRACK_ID = "track_id";
        public static final String COLUMN_NAME_SOUND_TYPE = "sound" ;
        public static final String COLUMN_NAME_ORDINAL = "ordinal" ;
    }
	
    static class MetaphorsDBHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "percussion.db";
        private static final int SCHEMA_VERSION = 1;

        MetaphorsDBHelper(Context context) {
            super(context, DATABASE_NAME, null, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            Log.d(TAG, "creating database " + DATABASE_NAME);

            /*
            db.execSQL("CREATE TABLE " + RhythmCategoryTable.TABLE_NAME + " ( " +
                    RhythmTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RhythmTable.COLUMN_NAME_TITLE + " TEXT); ");
                    */

            db.execSQL("CREATE TABLE " + RhythmTable.TABLE_NAME + " ( " +
            		RhythmTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
            		RhythmTable.COLUMN_NAME_TITLE + " TEXT, " +
                    RhythmTable.COLUMN_NAME_DESCRIPTION + " TEXT, " +
                    RhythmTable.COLUMN_NAME_CATEGORY + " TEXT, " + // INTEGER DEFAULT 1
                    RhythmTable.COLUMN_NAME_INTERNAL_FLAG + " INTEGER DEFAULT 0, " +
                    RhythmTable.COLUMN_NAME_HIDDEN_FLAG + " INTEGER DEFAULT 0, " +
                    RhythmTable.COLUMN_NAME_BIT_LENGTH + " INTEGER DEFAULT 4, " +
                    RhythmTable.COLUMN_NAME_BAR_BITS + " INTEGER DEFAULT 4, "+
            		RhythmTable.COLUMN_NAME_BPM + " INTEGER);");

            db.execSQL("CREATE TABLE " + TrackTable.TABLE_NAME + " ( " + 
            		TrackTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
            		TrackTable.COLUMN_NAME_TITLE + " TEXT, " + 
            		TrackTable.COLUMN_NAME_RHYTHM_ID + " INTEGER, " +
                    TrackTable.COLUMN_NAME_CONNECTED + " INTEGER DEFAULT 0, " +
                    TrackTable.COLUMN_NAME_BAR_CNT + " INTEGER DEFAULT 2, " +
                    TrackTable.COLUMN_NAME_INSTRUMENT + " INTEGER DEFAULT 0);");
            
            db.execSQL("CREATE TABLE " +
                    SoundTable.TABLE_NAME + " ( " +
                    SoundTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + 
                    SoundTable.COLUMN_NAME_TRACK_ID + " INTEGER, " +
                    SoundTable.COLUMN_NAME_ORDINAL + " INTEGER, " +
                    SoundTable.COLUMN_NAME_SOUND_TYPE + " INTEGER DEFAULT 0);");


            Log.d(TAG, "database is created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            recreateDatabase(db); //TODO : after release: do ALTER, not DROP-CREATE
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            recreateDatabase(db); //TODO : after release: do ALTER, not DROP-CREATE
        }
        
        private void recreateDatabase(SQLiteDatabase db) {
            dropTables(db);
            onCreate(db);
        }

        private void dropTables(SQLiteDatabase db) {
            Log.d(TAG, "Dropping database " + DATABASE_NAME);

            db.execSQL("DROP TABLE IF EXISTS " + RhythmTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TrackTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + SoundTable.TABLE_NAME);

            Log.d(TAG, "Database is dropped");
        }
    }

    public PercussionDatabase(Context context) {

        mOpenHelper = new MetaphorsDBHelper(context);
    }    
	
    public Cursor getAllRhythms(){
    	String query = "SELECT "+RhythmTable.COLUMN_NAME_TITLE+
                ", "+RhythmTable._ID +
                ", "+RhythmTable.COLUMN_NAME_CATEGORY +
                " FROM "+RhythmTable.TABLE_NAME +" WHERE "+RhythmTable.COLUMN_NAME_HIDDEN_FLAG+" =0";
    	return mOpenHelper.getReadableDatabase().rawQuery(query, null);
    }

    /**
     * Get list of all existing categories
     */
    public Cursor getCategories(){
        String query = "SELECT DISTINCT "+RhythmTable.COLUMN_NAME_CATEGORY+
                " FROM "+RhythmTable.TABLE_NAME;
        return mOpenHelper.getReadableDatabase().rawQuery(query, null);
    }

    public RhythmInfo getRhythmInfoById(long rhythmId){

    	SQLiteDatabase db = mOpenHelper.getReadableDatabase();

    	Cursor rhythmCursor = db.rawQuery("SELECT "+RhythmTable.COLUMN_NAME_TITLE+
                ", "+RhythmTable.COLUMN_NAME_BAR_BITS+
    			", "+RhythmTable.COLUMN_NAME_BIT_LENGTH+
                ", "+RhythmTable.COLUMN_NAME_CATEGORY+" " +
    			", "+RhythmTable._ID+" " +
                ", "+RhythmTable.COLUMN_NAME_DESCRIPTION+" " +
                ", "+RhythmTable.COLUMN_NAME_INTERNAL_FLAG+" " +
                ", "+RhythmTable.COLUMN_NAME_BPM+" " +
    			" FROM "+RhythmTable.TABLE_NAME+" g " +
    			" WHERE g._ID = ? ", 
    			new String[]{rhythmId+""});
    	
    	RhythmInfo rhythmInfo = null;
    	
    	while (rhythmCursor.moveToNext()) {
    		Log.d(TAG, "Rhythm with ID "+rhythmId+" is found. Processing...");

    		rhythmInfo = new RhythmInfo(rhythmCursor.getLong(4), rhythmCursor.getString(0), rhythmCursor.getShort(1), rhythmCursor.getShort(2), rhythmCursor.getString(5), rhythmCursor.getString(3), rhythmCursor.getInt(6));
            rhythmInfo.setBpm(rhythmCursor.getInt(7));//its not really a rhythm attribute, so it is not in constructor

    		Cursor trackCursor = db.rawQuery("SELECT "+TrackTable.COLUMN_NAME_TITLE+
        			", "+TrackTable.COLUMN_NAME_INSTRUMENT+
                    ", "+TrackTable.COLUMN_NAME_BAR_CNT+
                    ", "+TrackTable.COLUMN_NAME_CONNECTED+
        			", "+TrackTable._ID+
        			" FROM "+TrackTable.TABLE_NAME+" t " +
        			" WHERE t."+TrackTable.COLUMN_NAME_RHYTHM_ID+" = ? ", 
        			new String[]{rhythmCursor.getLong(4)+""});

        	while (trackCursor.moveToNext()) {
        		Log.d(TAG, "Track with ID "+trackCursor.getLong(4)+" is found. Processing...");
        		
        		Instruments instrument = Instruments.values()[trackCursor.getInt(1)];
        		
        		TrackInfo trackInfo = new TrackInfo(trackCursor.getLong(4), trackCursor.getString(0), instrument, trackCursor.getInt(2), trackCursor.getInt(3));

        		Cursor soundsCursor = db.rawQuery("SELECT "+SoundTable.COLUMN_NAME_SOUND_TYPE+
            			", "+SoundTable._ID+
                        ", "+SoundTable.COLUMN_NAME_ORDINAL+
            			" FROM "+SoundTable.TABLE_NAME+" s " +
            			" WHERE s."+SoundTable.COLUMN_NAME_TRACK_ID+" = ? ORDER BY "+SoundTable.COLUMN_NAME_ORDINAL+" ASC", 
            			new String[]{trackCursor.getLong(4)+""});

        		SoundInfo [] sounds = new SoundInfo[trackInfo.getBarCnt() * rhythmInfo.getSoundNumberForBar()];
            	while (soundsCursor.moveToNext()) {
            		Log.d(TAG, "Sound found. int type: "+soundsCursor.getInt(0)+". enum type: "+ InstrumentFactory.getSoundById(soundsCursor.getInt(0), instrument));
                    if (soundsCursor.getInt(0) == -1) continue;
            		sounds[soundsCursor.getInt(2)] = new SoundInfo(InstrumentFactory.getSoundById(soundsCursor.getInt(0),instrument));
            	}
        		
            	trackInfo.appendSoundArray( sounds );
                rhythmInfo.addTrack(trackInfo);

        	}

    	}

    	Log.d(TAG, "Rhythm with ID "+rhythmId+" is loaded from DB. Result: "+rhythmInfo);
    	
		return rhythmInfo;
    }
    
    public long cloneRhythm(RhythmInfo rhythmInfo){
    	rhythmInfo.setId(-1);
    	return saveRhythm(rhythmInfo, false);
    }

    public long cloneRhythmById(long rhythmId){
        RhythmInfo rhythmInfo = getRhythmInfoById(rhythmId);
        rhythmInfo.setTitle( rhythmInfo.getTitle() + " (Copy)" );
        rhythmInfo.setInternalFlag(0);
        rhythmInfo.setId(-1);
        return saveRhythm(rhythmInfo, false);
    }


    public void removeRhythmById(long rhythmId){

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	
        Log.d(TAG, "Going to remove rhythm with ID: "+rhythmId);

        db.rawQuery("DELETE FROM "+SoundTable.TABLE_NAME+" WHERE "+SoundTable.COLUMN_NAME_TRACK_ID +" IN (" +
    			" SELECT "+TrackTable._ID+" FROM "+TrackTable.TABLE_NAME+" WHERE "+TrackTable.COLUMN_NAME_RHYTHM_ID+" = ?) ", 
    			new String[]{rhythmId + ""});
        
        db.delete(TrackTable.TABLE_NAME, TrackTable.COLUMN_NAME_RHYTHM_ID + " = ?" , new String[]{rhythmId + ""} );
        db.delete(RhythmTable.TABLE_NAME, RhythmTable._ID + " = ?" , new String[]{rhythmId + ""} );
    }

    public long saveRhythmsState(RhythmInfo rhythmInfo, long tmpRhythmId) {
        rhythmInfo.setId(tmpRhythmId); //if -1, then do insert
        return saveRhythm(rhythmInfo, true);
    }

    public long saveRhythm(RhythmInfo rhythmInfo, boolean isHidden){
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	
        Log.d(TAG, "Going to save rhythm: "+rhythmInfo);
        
        long rhythmId = rhythmInfo.getId();
        ContentValues cv = new ContentValues();

        cv.put(RhythmTable.COLUMN_NAME_TITLE, rhythmInfo.getTitle());
        cv.put(RhythmTable.COLUMN_NAME_DESCRIPTION, rhythmInfo.getDescription());
        cv.put(RhythmTable.COLUMN_NAME_BAR_BITS, rhythmInfo.getBitsPerBar());
        cv.put(RhythmTable.COLUMN_NAME_CATEGORY, rhythmInfo.getCategory());
        cv.put(RhythmTable.COLUMN_NAME_HIDDEN_FLAG, isHidden?1:0);
        cv.put(RhythmTable.COLUMN_NAME_BPM, rhythmInfo.getBpm());
        cv.put(RhythmTable.COLUMN_NAME_BIT_LENGTH, rhythmInfo.getBitLength());

        if (rhythmId != -1) {
        	
        	// Rhythm info is updated, track info is deleted and inserted again
            db.update(RhythmTable.TABLE_NAME, cv,RhythmTable._ID + " = ?" , new String[]{rhythmInfo.getId() + ""});

        	db.rawQuery("DELETE FROM "+SoundTable.TABLE_NAME+" WHERE "+SoundTable.COLUMN_NAME_TRACK_ID +" IN (" +
        			" SELECT "+TrackTable._ID+" FROM "+TrackTable.TABLE_NAME+" WHERE "+TrackTable.COLUMN_NAME_RHYTHM_ID+" = ?) ", 
        			new String[]{rhythmInfo.getId() + ""});
            
	        db.delete(TrackTable.TABLE_NAME, TrackTable.COLUMN_NAME_RHYTHM_ID + " = ?" , new String[]{rhythmInfo.getId() + ""} );

	        Log.d(TAG," Rhythm with ID " + rhythmId +" is updated");
	        
        } else {

	        rhythmId = db.insert(RhythmTable.TABLE_NAME, null, cv);
	        
	        Log.d(TAG," Rhythm with ID " + rhythmId +" is saved");
        }
        
    	for (int i=0; i<rhythmInfo.getTracks().size();i++){

    		SoundInfo [] sounds = rhythmInfo.getTracks().get(i).getSounds();
    		
    		cv = new ContentValues();
            cv.put(TrackTable.COLUMN_NAME_TITLE, rhythmInfo.getTracks().get(i).getTitle());
            cv.put(TrackTable.COLUMN_NAME_RHYTHM_ID, rhythmId);
            cv.put(TrackTable.COLUMN_NAME_CONNECTED, rhythmInfo.getTracks().get(i).isConnectedPrev()?1:0);
            cv.put(TrackTable.COLUMN_NAME_INSTRUMENT, rhythmInfo.getTracks().get(i).getInstrument().ordinal());
            cv.put(TrackTable.COLUMN_NAME_BAR_CNT, rhythmInfo.getTracks().get(i).getBarCnt());

            long trackId = db.insert(TrackTable.TABLE_NAME, null, cv);

            Log.d(TAG," Track with ID " + trackId +" is saved for rhythm id = "+rhythmId+". number of sounds: "+sounds.length);

            cv = new ContentValues();

            for (int j=0; j<sounds.length;j++){
                cv.clear();
        		if (sounds[j] == null) {
                    Log.d(TAG," Sound with idx " + j +" is NULL.");
                    cv.put(SoundTable.COLUMN_NAME_SOUND_TYPE, -1);
        		} else
        			cv.put(SoundTable.COLUMN_NAME_SOUND_TYPE, sounds[j].getSound().getIndex());
                cv.put(SoundTable.COLUMN_NAME_TRACK_ID, trackId);
                cv.put(SoundTable.COLUMN_NAME_ORDINAL, j);
                long sndId = db.insert(SoundTable.TABLE_NAME, null, cv);
                Log.d(TAG," Sound with ID " + sndId +" of type "+sounds[j]+" is saved for track: "+trackId);
        	}
    		
    	}
    	
        db.close();
        
        return rhythmId;
    }
	
}
