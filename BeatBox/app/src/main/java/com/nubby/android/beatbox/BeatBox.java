package com.nubby.android.beatbox;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeatBox {
    private static final String TAG = "BeatBox";
    private static final String SOUNDS_FOLDER = "sample_sounds";
    private static final int MAX_SOUNDS = 5;

    private AssetManager mAssetManager;
    private List<Sound> mSounds = new ArrayList<>();
    private SoundPool mSoundPool;
    private float mSpeed; //from 0.5 to 2;

    public BeatBox(Context context) {
        mAssetManager = context.getAssets();
        mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        mSpeed = 1;
        loadSounds();
    }

    private void load(Sound sound) throws IOException {
        AssetFileDescriptor fileDescriptor = mAssetManager.openFd(sound.getAssetPath());
        int soundId = mSoundPool.load(fileDescriptor, 0);
        sound.setSoundId(soundId);
    }

    private void loadSounds(){
        List<String> paths = new ArrayList<>();
        try {
            paths = Arrays.asList(mAssetManager.list(SOUNDS_FOLDER));
            Log.i(TAG, "Found " + mSounds.size());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Cant list assets ", e);
        }
        for (String file: paths) {
            Sound sound = new Sound(SOUNDS_FOLDER + "/" + file);
            mSounds.add(sound);
            try {
                load(sound);
                Log.i(TAG, "Loaded " + sound.getName());
            } catch (IOException e) {
                Log.e(TAG, "Cant load asset " + file, e);
                e.printStackTrace();
            }
        }
    }

    public List<Sound> getSounds(){
        return mSounds;
    }

    public void play(Sound sound) {
        Integer soundId = sound.getSoundId();
        if (soundId == null) return;
        mSoundPool.play(soundId, 1, 1, 1, 0, mSpeed);
    }

    public void release() {
        mSoundPool.release();
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }
}
