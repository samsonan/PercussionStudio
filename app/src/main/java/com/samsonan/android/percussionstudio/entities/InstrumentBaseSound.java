package com.samsonan.android.percussionstudio.entities;

public interface InstrumentBaseSound {

    public int getHotpotColor();        // which color in hotpot map is associated with this sound

	public int getSoundResourceId();    //android sound raw resource id
	
	public int getIndex();              //sound index in enum
	
	public String getDisplayText();     // what symbol be displayed in UI to display this sound
	
	public int getButtonId();           // android button id

    public InstrumentFactory.Instrument getInstrument();   //which instrument this sound belongs to???

}
