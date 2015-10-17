package com.badlogic.dmp;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;


public class DMP implements ApplicationListener {
    private Texture marioImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Rectangle bucket;
    private Array<Sprite> marioparts;
    private long lastDropTime;

    @Override
    public void create() {
        // load the images for mario (512x512) and the bucket (64x64)
        marioImage = new Texture(Gdx.files.internal("mario.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // start the playback of the background music immediately
        rainMusic.setLooping(true);
        rainMusic.play();

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();

        // create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
        bucket.width = 64;
        bucket.height = 64;

        // create the raindrops array and spawn the first raindrop
        marioparts = new Array<Sprite>();
        spawnMarioParts();
    }

    private void spawnMarioParts() {
        // Choose random 64x64 square within 512x512 marioImage texture:
        // (TextureRegion coordinate system is Java2D's default coordinate system)
        int part_x = MathUtils.random(0, 512 - 64);
        int part_y = MathUtils.random(0, 512 - 64);
        int part_width = 64;
        int part_height = 64;
        Sprite partrect = new Sprite(marioImage, part_x, part_y, part_width, part_height);

        // Choose random starting position at top of screen for partrect Sprite:
        int partrect_x = MathUtils.random(0, 800 - 64);
        int partrect_y = 480;
        partrect.setPosition(partrect_x, partrect_y);

        marioparts.add(partrect);
        lastDropTime = TimeUtils.nanoTime();
    }







    @Override
    public void render() {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        batch.setProjectionMatrix(camera.combined);

        // begin a new batch and draw the bucket and
        // all drops (mario parts)
        batch.begin();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for (Sprite mariopart : marioparts) {
            mariopart.draw(batch);
        }
        batch.end();

        // process user input
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // make sure the bucket stays within the screen bounds
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > 800 - 64) bucket.x = 800 - 64;

        // check if we need to create a new raindrop
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnMarioParts();

        // move the drops (mario parts), remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the later case we play back
        // a sound effect as well.
        Iterator<Sprite> iter = marioparts.iterator();
        while (iter.hasNext()) {
            Sprite mariopart = iter.next();
            float mariopart_next_y = (mariopart.getY() - 200 * Gdx.graphics.getDeltaTime());
            mariopart.setY(mariopart_next_y);
            if (mariopart_next_y + 64 < 0) iter.remove();
            // Create a Rectangle for each mariopart to check for collision with bucket rectangle:
            Rectangle partrect = new Rectangle(mariopart.getX(), mariopart_next_y, mariopart.getWidth(), mariopart.getHeight());
            if (partrect.overlaps(bucket)) {
                dropSound.play();
                iter.remove();
            }
        }
    }

    @Override
    public void dispose() {
        // dispose of all the native resources
        marioImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}