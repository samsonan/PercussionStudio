package com.samsonan.android.percussionstudio.views;

import java.util.Arrays;
import java.util.HashMap;

import com.samsonan.android.percussionstudio.R;
import com.samsonan.android.percussionstudio.entities.RhythmInfo;
import com.samsonan.android.percussionstudio.entities.SoundInfo;
import com.samsonan.android.percussionstudio.entities.TrackInfo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 *  View to display rhythms and tracks and sounds
 */
public class TrackView extends View {

    private static final String TAG = "TrackView";

    /** ====================== View Display Constant Values ========================================
    /**  values in pixels, for mdpi. before used, should be translated according to actual density! **/
    private final static int PADDING = 5; // (each) track padding
    private final static int TRACK_HEADER_PADDING = 5; // track header text (title and info) padding
    private final static int BUTTON_SPACE = 38; //space for buttons (i.e. play button) before (each) track
    private final static int TRACK_HEIGHT = 65; //track height (not including header/title)
    private final static int TRACK_TITLE_HEIGHT = 24;
    private final static int TRACK_BAR_HEIGHT = 24; //selectable area of the measure bar
    private final static int BIT_SQUARE_WIDTH = 25; // width of a bit square
    private final static int SQUARE_TEXT_PADDING = 7; // text padding inside the bit square
    private final static int MARKER_THICKNESS = 4; // play/selection marker thickness
    private final static int MAIN_TEXT = 24; //
    private final static int SMALL_TEXT = 12; //

    private final static int PANEL_TRANSLATION_CORRECTION = 200; //

    /**
     * ============================  Drawing related vars ==========================================
     */

    private float mDensity = 1; //density factor. 1.0 for mdpi, 2.0 for xhdpi
    private boolean mIsBigScreen = false; //big screens have separate layout and view drag behavior

    private Bitmap mPlayButton, mAddTrackButton;
    private Paint mTrackButtonsPaint, mSelectedBitPaint, mPlayedBitPaint, mTrackSupplLinesPaint,
            mTextPaint, mTrackInfoTextPaint, mTrackBarBg, mTrackTitleBg,
            mSelectedBarPaint, mAddTrackBg;

    private float mBottomDrawingEnd; //lowest pixel drawn in our view - for drag/pan control

    /**
     * ============================ Position and Drag/Pan related vars =============================
     */

    /**
     * We need to know view width and height to determine allowable dragging/panning distance
     */
    private int mViewWidth = 0;
    private int mViewHeight = 0;

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f;
    private float startY = 0f;

    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0f;
    private float translateY = 0f;

    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;

    //X coords of all dound bits for all tracks
    private HashMap<Integer, float[]> mPositionMap;

    /**
     * =========================== Content related vars ============================================
     **/

    private RhythmInfo mRhythmInfo = null; //rhythm being edited

    private boolean mIsOneTrackPlaying;
    private int mPlayPosition = 0; //current play position
    private int mPlayTrack = -1; //track being played. 0 for first, etc

    /* currently selected position, bar and track*/
    private int mSelectedPosition = -1;
    private int mSelectedTrack = -1;
    private int mSelectedBar = -1;


    public interface FragmentContainerListener {

        public boolean isAddBarOnTrackEnd();

        public void onAddTrack();

        public void onAddBarToTrack(int trackIdx);

        public void onPlayTrack(int trackIdx);

        public void onPositionSelected(int positionIdx, int trackIdx);

        public void onBarSelected(int barIdx, int trackIdx);

        public void onTrackHeaderSelected(int trackIdx);
    }

    private FragmentContainerListener mFragmentListenerCallback;  //fragment container

    public void setFragmentContainerListener(FragmentContainerListener mCallback) {
        mFragmentListenerCallback = mCallback;
    }

    /*
     *   is view in edit mode?
     */
    private boolean mIsEditable = false;

    public void setIsEditable(boolean isEditable) {
        mIsEditable = isEditable;
    }

    /*
     * is currently played ?
     */
    private boolean mIsPlayed = false;

    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initResources();
    }

    public TrackView(Context context) {
        super(context);
        initResources();
    }

    /**
     * Invoked when new rhythm is loaded / updated in parent activity
     */
    public void setRhythmInfo(RhythmInfo rhythmInfo, boolean layoutChange) {
        this.mRhythmInfo = rhythmInfo;
        calculateBitsPosition(); //calculate every bit position in advance
        invalidate();
        if (layoutChange)
            requestLayout();
    }

    /**
     * Calculate track sound coordinates
     */
    private void calculateBitsPosition() {
        mPositionMap = new HashMap<>();

        for (int i = 0; i < mRhythmInfo.getTrackCnt(); i++) {
            SoundInfo[] sounds = mRhythmInfo.getTrackIdx(i).getSounds();
            float[] positionArrayX = new float[sounds.length + 1]; //+1 for the right side of the last bit

            for (int j = 0; j < mRhythmInfo.getTrackIdx(i).getBarCnt(); j++) {
                for (int k = 0; k < (mRhythmInfo.getSoundNumberForBar()); k++)
                    positionArrayX[j * mRhythmInfo.getSoundNumberForBar() + k] = mDensity * ( PADDING + BUTTON_SPACE + (j * mRhythmInfo.getSoundNumberForBar() + k) * BIT_SQUARE_WIDTH );
            }

            //position of the right side of the last bit
            positionArrayX[positionArrayX.length - 1] = positionArrayX[positionArrayX.length - 2] + mDensity * BIT_SQUARE_WIDTH;

            Log.d(TAG, "calculateBitsPosition(). bit positions for track " + i + ":" + Arrays.toString(positionArrayX));

            mPositionMap.put(i, positionArrayX);
        }
    }

    /**
     * initialize graphic resources on start
     */
    private void initResources() {

        DisplayMetrics dm = new DisplayMetrics();

        if (isInEditMode())
            return;

        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;

        /*
        int widthPixels = dm.widthPixels;
        int heightPixels = dm.heightPixels;
        float widthDp = widthPixels / mDensity;
        float heightDp = heightPixels / mDensity;
        float mSmallestWidth = Math.min(widthDp, heightDp);
        */
        mIsBigScreen = getResources().getBoolean(R.bool.is_big_screen);
        Log.d(TAG, "initResources(). screen density:" + mDensity+", mIsBigScreen:"+mIsBigScreen);

        mPlayButton = BitmapFactory.decodeResource(getResources(), R.drawable.ic_play_circle_24dp);

        mAddTrackButton = BitmapFactory.decodeResource(getResources(), R.drawable.ic_plus_24dp);

        //paint to draw buttons
        mTrackButtonsPaint = new Paint();
        mTrackButtonsPaint.setAntiAlias(true);

        //paint to draw selection marker
        mSelectedBitPaint = new Paint();
        mSelectedBitPaint.setStyle(Style.STROKE);
        mSelectedBitPaint.setStrokeJoin(Join.ROUND);
        mSelectedBitPaint.setColor(Color.DKGRAY);
        mSelectedBitPaint.setStrokeWidth(MARKER_THICKNESS * mDensity);

        //paint to draw play marker
        mPlayedBitPaint = new Paint();
        mPlayedBitPaint.setStyle(Style.STROKE);
        mPlayedBitPaint.setStrokeJoin(Join.ROUND);
        mPlayedBitPaint.setColor(Color.BLUE);
        mPlayedBitPaint.setStrokeWidth(MARKER_THICKNESS * mDensity);

        //paint to draw track bar lines
        mTrackSupplLinesPaint = new Paint();
        mTrackSupplLinesPaint.setColor(Color.BLACK);

        //main text, used to draw sounds
        mTextPaint = new Paint();
        mTextPaint.setTextSize(MAIN_TEXT * mDensity);
        mTextPaint.setColor(Color.BLACK);

        //small text, used to draw track info
        mTrackInfoTextPaint = new Paint();
        mTrackInfoTextPaint.setTextSize(SMALL_TEXT * mDensity);
        mTrackInfoTextPaint.setColor(Color.BLACK);

        //track header
        mTrackTitleBg = new Paint();
        mTrackTitleBg.setStyle(Style.FILL);
        mTrackTitleBg.setColor(getResources().getColor(R.color.track_title_bg));

        //add track button background
        mAddTrackBg = new Paint();
        mAddTrackBg.setStyle(Style.FILL);
        mAddTrackBg.setColor(getResources().getColor(R.color.add_track_bg));

        //track measure bar
        mTrackBarBg = new Paint();
        mTrackBarBg.setStyle(Style.FILL);
        mTrackBarBg.setColor(getResources().getColor(R.color.track_bar_bg));

        //selected track or measure bar paint
        mSelectedBarPaint = new Paint();
        mSelectedBarPaint.setStyle(Style.FILL);
        mSelectedBarPaint.setColor(getResources().getColor(R.color.track_bar_sel_bg));

    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout. changed:" + changed + ", bottom:" + bottom + ". right:" + right);
        mViewWidth = right;
        mViewHeight = bottom;
    }

    @Override
    public void onDraw(Canvas canvas) {

        if(isInEditMode()) {
            Paint paint = new Paint();
            paint.setColor(Color.GRAY);
            canvas.drawRect(0, 0, mViewWidth,mViewHeight, paint);
            return;
        }

        int bigScreenHeightPanelCorrection = 0;
        if(mIsBigScreen)
            bigScreenHeightPanelCorrection = PANEL_TRANSLATION_CORRECTION * 2;

        /**
         * Rightmost and the lowest drawing position of all tracks and buttons.
         * Used to determine allowable dragging/panning distance in relate to the view bounds
         */
        float mRightmostDrawingEnd = mDensity * (PADDING + BUTTON_SPACE + BIT_SQUARE_WIDTH * mRhythmInfo.getSoundNumberForBar() * mRhythmInfo.getMaxBarCnt() + BUTTON_SPACE);
        mBottomDrawingEnd = mDensity * ((mRhythmInfo.getTrackCnt()) * (2 * PADDING + TRACK_HEIGHT + TRACK_TITLE_HEIGHT) + TRACK_HEIGHT * 2f / 3);

        if (mRightmostDrawingEnd < mViewWidth) { // if the view fits the screen - don't allow to move at all (X axis)
            translateX = 0;
        } else if (translateX > 0) { // taking care of the left bound (not allowed to move tracks to the right)
            translateX = 0;
        } else if (translateX < mViewWidth - mRightmostDrawingEnd) { // taking care of the left bound (not allowed to move too much to the left)
            translateX = mViewWidth - mRightmostDrawingEnd;
        }

        if (mBottomDrawingEnd < mViewHeight - bigScreenHeightPanelCorrection) { // if the view fits the screen - don't allow to move at all (Y axis)
            translateY = 0;
        } else if (translateY > 0) { // taking care of the top bound (not allowed to move tracks down)
            translateY = 0;
        } else if (translateY < mViewHeight - bigScreenHeightPanelCorrection - mBottomDrawingEnd) { // taking care of the bottom bound (not allowed to move too much up)
            translateY = mViewHeight - bigScreenHeightPanelCorrection - mBottomDrawingEnd;
        }

        canvas.translate(translateX, translateY);//move canvas according to the pan distance

        // what to display in track header if the track is connected to prev.track
        String playTogetherStr = getResources().getString(R.string.track_play_together_info);

        /**
         * if tracks are connected, then we have to play them together. As we draw tracks one by one,
         * we need to keep the position of the last known connected played track
         */
        int lastConnectedPlayedTrack = -1;

        for (int i = 0; i < mRhythmInfo.getTrackCnt(); i++) {

            TrackInfo trackInfo = mRhythmInfo.getTrackIdx(i);
            float [] positionArrayX = mPositionMap.get(i);

            float trackTopEnd = mDensity * ( PADDING + TRACK_TITLE_HEIGHT + i * (TRACK_TITLE_HEIGHT + TRACK_HEIGHT + PADDING * 2) );
            float trackBottomEnd = trackTopEnd + mDensity * TRACK_HEIGHT;
            float trackRightEnd = mDensity * ( PADDING + BUTTON_SPACE + BIT_SQUARE_WIDTH * (mRhythmInfo.getSoundNumberForBar() * trackInfo.getBarCnt()) );

            // track header
            canvas.drawRect(mDensity * PADDING, trackTopEnd - mDensity * TRACK_TITLE_HEIGHT, trackRightEnd, trackTopEnd,
                    (mSelectedTrack == i && mSelectedBar == -1 && mSelectedPosition == -1) ? mSelectedBarPaint : mTrackTitleBg);

            // track title and info
            canvas.drawText("x"+trackInfo.getPlayTimes() +" "+ (trackInfo.isConnectedPrev() ? playTogetherStr : " ")+ " [" + trackInfo.getInstrument().name() + "] "+trackInfo.getTitle(),
                    PADDING * 2 * mDensity, trackTopEnd - mDensity * TRACK_HEADER_PADDING, mTrackInfoTextPaint);

            //rightmost measure bar of the track
            canvas.drawLine(trackRightEnd, trackTopEnd, trackRightEnd, trackBottomEnd, mTrackSupplLinesPaint);

            // draw play button - before track
            canvas.drawBitmap(mPlayButton, mDensity * PADDING, trackTopEnd + mDensity * TRACK_HEIGHT/2, mTrackButtonsPaint);

            for (int j = 0; j < trackInfo.getBarCnt(); j++) {

                int positionOffset = mRhythmInfo.getSoundNumberForBar() * j;

                float barLeftEnd = mDensity * ( PADDING + BUTTON_SPACE + BIT_SQUARE_WIDTH * positionOffset );

                //draw left border of each measure bar
                canvas.drawLine(barLeftEnd, trackTopEnd, barLeftEnd, trackBottomEnd, mTrackSupplLinesPaint);

                // draw measure bar header
                canvas.drawRect(barLeftEnd, trackTopEnd,
                        barLeftEnd + mDensity * mRhythmInfo.getSoundNumberForBar() * BIT_SQUARE_WIDTH,
                        trackTopEnd + mDensity * TRACK_BAR_HEIGHT, //TRACK_BAR_HEIGHT is also the height of the bar line
                        (mSelectedTrack == i && mSelectedBar == j) ? mSelectedBarPaint : mTrackBarBg);//if this bar is selected or not?

                for (int k = 0; k < mRhythmInfo.getSoundNumberForBar(); k++) {

                    //marker left end
                    float markerLeftEnd = mDensity * ( PADDING + BUTTON_SPACE + BIT_SQUARE_WIDTH * (k + positionOffset) );

                    // draw current position marker
                    if (!mIsPlayed) {

                        if ((k + positionOffset) == mSelectedPosition && i == mSelectedTrack) {
                            canvas.drawLine(markerLeftEnd, trackBottomEnd, markerLeftEnd + mDensity * BIT_SQUARE_WIDTH, trackBottomEnd, mSelectedBitPaint);
                        }
                    }

                    // draw play marker
                    if (mIsPlayed && (k + positionOffset) == mPlayPosition) { // this sound position is currently played...
                        //we playing the whole rhythm, current track is connected to previous, which was also connected and played!
                        if (!mIsOneTrackPlaying && trackInfo.isConnectedPrev() && i - 1 == lastConnectedPlayedTrack) {
                            lastConnectedPlayedTrack = i;   //setting the marker to this track, which means that it has to be played as well
                        }

                        if (mPlayTrack == i || lastConnectedPlayedTrack == i) {
                            lastConnectedPlayedTrack = i;
                            canvas.drawLine(markerLeftEnd, trackBottomEnd, markerLeftEnd + mDensity * BIT_SQUARE_WIDTH, trackBottomEnd, mPlayedBitPaint);
                        }
                    }

                    SoundInfo s = trackInfo.getSounds()[k + positionOffset];
                    canvas.drawText(s == null ? "-" : s.getSound().getDisplayText(), mDensity * SQUARE_TEXT_PADDING + positionArrayX[k + positionOffset], trackBottomEnd - mDensity * SQUARE_TEXT_PADDING, mTextPaint);
                }
            }

            if (mIsEditable) {

                canvas.drawRect(mDensity * PADDING, mBottomDrawingEnd - mDensity * TRACK_HEIGHT * 2f / 3,
                        mRightmostDrawingEnd, mBottomDrawingEnd, mAddTrackBg);

                canvas.drawBitmap(mAddTrackButton, mDensity * PADDING,
                        mBottomDrawingEnd - mDensity * TRACK_HEIGHT *3 /5 , mTrackButtonsPaint);

                canvas.drawText(getResources().getString(R.string.btn_add_track),
                        mDensity * ( PADDING + BUTTON_SPACE ), mBottomDrawingEnd- mDensity * TRACK_HEIGHT / 4, mTrackInfoTextPaint);
            }

        }
    }

    /**
     * Set current playing position
     *
     * Should be invoked by playing timer (outside this view)
     *
     * @param current current position
     * @param trackIdx current track
     */
    public void setCurrentPlayPosition(int current, int trackIdx) {

        this.mPlayPosition = current;
        this.mPlayTrack = trackIdx;
    }

    /**
     * Set view to playing mode. It affects what information (which markers) will be displayed.
     *
     * Invoked in container fragment when play&stop buttons are pressed
     *
     * @param isPlayed if is in playing mode
     * @param isOneTrackPlaying if we play one particular track or the whole rhythm
     */
    public void onPlayStopRhythm(boolean isPlayed, boolean isOneTrackPlaying) {

        Log.d(TAG, "Playing rhythm");

        this.mIsPlayed = isPlayed;
        mIsOneTrackPlaying = isOneTrackPlaying;
        mPlayPosition = 0;  //reset playing position
    }

    /**
     * Play particular track
     * @param trackIdx track index in tracks array
     */
    private void onPlayTrack(int trackIdx) {
        Log.d(TAG, "Playing track " + trackIdx);
        //delegate event to the fragment. Fragment will call TrackView.onPlayStopRhythm(boolean isPlayed, boolean isOneTrackPlaying)
        mPlayTrack = trackIdx;
        mFragmentListenerCallback.onPlayTrack(trackIdx);
    }

    private boolean isDrag;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {

        final int action = ev.getAction();

        if (MotionEvent.ACTION_UP == action) {

            //All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
            //previousTranslate
            previousTranslateX = translateX;
            previousTranslateY = translateY;

            float x = ev.getX() - translateX;
            float y = ev.getY() - translateY;

            //if we were dragging, then don't process button clicks
            if (isDrag) {
                isDrag = false;
                return true;
            }

            /**
             * Note: these coordinates are relative to the view!
             *
             * We clicked somhere in the track are
             */
            if (y > 0 && y < mDensity * (2 * PADDING + TRACK_HEIGHT + TRACK_TITLE_HEIGHT) * mRhythmInfo.getTrackCnt()) {

                int trackIdx = -1;

                float previousTracksBottom = 0;
                float fullTrackHeight = mDensity * (2 * PADDING + TRACK_HEIGHT + TRACK_TITLE_HEIGHT);

                //find out which track was clicked
                for (int j = 1; j < mRhythmInfo.getTrackCnt() + 1; j++) {
                    if (y < j * fullTrackHeight) {
                        previousTracksBottom = (j - 1) * fullTrackHeight;
                        trackIdx = j - 1;
                        break;
                    }
                }


                Log.d(TAG, "User clicked (" + x + "," + y + ") on track #" + trackIdx + ". previousTracksBottom:" + previousTracksBottom);

                float[] positionArrayX = mPositionMap.get(trackIdx);

                if (y < previousTracksBottom + mDensity * (PADDING + TRACK_TITLE_HEIGHT)) {
                    Log.d(TAG, "User clicked on track title");
                    if (mSelectedTrack == trackIdx && mSelectedBar == -1 && mSelectedPosition == -1) { //this track is selected. deselect
                        setSelectedTrack(-1);
                        mFragmentListenerCallback.onTrackHeaderSelected(-1);
                    } else {
                        setSelectedTrack(trackIdx);
                        mFragmentListenerCallback.onTrackHeaderSelected(mSelectedTrack);
                    }
                    invalidate(); // redraw because track header color should be changed
                    return true;
                }

                if (y < previousTracksBottom + mDensity * (PADDING + TRACK_TITLE_HEIGHT + TRACK_BAR_HEIGHT)) {
                    Log.d(TAG, "User clicked on bar header");
                    for (int i = 0; i < positionArrayX.length; i++) {
                        if (x > mDensity * (PADDING + BUTTON_SPACE) && x < (positionArrayX[i])) {
                            int barNum = (i - 1) / mRhythmInfo.getSoundNumberForBar();
                            Log.d(TAG, "bar #" + barNum + " selected (pos=" + (i - 1) + ")");

                            if (mSelectedBar == barNum && mSelectedTrack == trackIdx) { //already selected. deselect bar
                                setSelectedBar(-1, -1);
                                mFragmentListenerCallback.onBarSelected(-1, -1);
                            } else {
                                setSelectedBar(barNum, trackIdx);
                                mFragmentListenerCallback.onBarSelected(mSelectedBar, mSelectedTrack);
                            }
                            invalidate();  // redraw because measure bar color should be changed
                            return true;
                        }
                    }
                }

                //track play button
                if (x < positionArrayX[0]) {
                    onPlayTrack(trackIdx);
                    return true;
                }

                /**
                 *  Processing sound bits clicks
                 */
                for (int i = 0; i < positionArrayX.length; i++) {

                    if ( x < positionArrayX[i] ) {

                        setSelectedPosition(i - 1, trackIdx);
                        Log.d(TAG, "Sound click. Sound bit #" + mSelectedPosition + " of track " + mSelectedTrack + " is selected.");

                        mFragmentListenerCallback.onPositionSelected(mSelectedPosition, mSelectedTrack);

                        invalidate();
                        return true;
                    }
                }
            }
            if (y > 0 && y < mBottomDrawingEnd && mIsEditable) {
                Log.d(TAG, "Add track button is clicked.");
                mFragmentListenerCallback.onAddTrack();
                return true;
            }
        }

        if (MotionEvent.ACTION_DOWN == action) {


            //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
            //amount for each coordinates This works even when we are translating the first time because the initial
            //values for these two variables is zero.
            startX = ev.getX() - previousTranslateX;
            startY = ev.getY() - previousTranslateY;

        }

        if (MotionEvent.ACTION_MOVE == action) {

            translateX = ev.getX() - startX;
            translateY = ev.getY() - startY;


            // Perhaps we should calculate dragging distance in order to set isDrag flag ???
            double distance = Math.sqrt(Math.pow(ev.getX() - (startX + previousTranslateX), 2) +
                            Math.pow(ev.getY() - (startY + previousTranslateY), 2)
            );

            if (distance > mDensity * BIT_SQUARE_WIDTH)
                isDrag = true;

            invalidate();

        }
        return true;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public int getSelectedTrack() {
        return mSelectedTrack;
    }

    public int getSelectedBar() {
        return mSelectedBar;
    }

    public void incrementSelectedPosition() {

        setSelectedPosition(++mSelectedPosition, mSelectedTrack);

    }

    public void setSelectedPosition(int position, int trackIdx) {

        /**
         * If selected position if further tha actual number of sounds, add new measure bar!
         */
        if (position >= mRhythmInfo.getSoundNumberForBar() * mRhythmInfo.getTrackIdx(trackIdx).getBarCnt()) {
            if (mFragmentListenerCallback.isAddBarOnTrackEnd()) {
                mFragmentListenerCallback.onAddBarToTrack(trackIdx);
                invalidate();
            } else
                position=0;
        }

        this.mSelectedPosition = position;
        mSelectedTrack = trackIdx;
        this.mSelectedBar = -1;
    }

    public void setSelectedTrack(int trackIdx) {
        this.mSelectedPosition = -1;
        mSelectedTrack = trackIdx;
        this.mSelectedBar = -1;
    }

    public void setSelectedBar(int barNum, int trackIdx) {
        this.mSelectedPosition = -1;
        mSelectedTrack = trackIdx;
        this.mSelectedBar = barNum;
    }

}
