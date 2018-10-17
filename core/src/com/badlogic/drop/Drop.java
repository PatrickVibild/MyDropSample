package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
//check what this is.
import java.util.Iterator;




public class Drop extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	//variable for our bucket. limit the area of our screen
	private Rectangle bucket;
	//will track the position of our raindrops
	private Array<Rectangle> raindrops;
	//check the last time a raindrop droped, time in nanoseconds
	private long lastDropTime;


	//create all our items needed for our game
	@Override
	public void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.wav"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

        // creates a camera that follows instances the map on 800 x 480 pixels
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		//we create a spritebatch, whatever that means...
		batch = new SpriteBatch();
		//we create the rectangle that will be our bucket
		bucket = new Rectangle();
		//limit wwhere our rectangle can move our game world
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		//size of our bucket.
		bucket.width = 64;
		bucket.height = 64;
		//we create our first raindrop
		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	//generate all our videos
	@Override
	public void render () {
		//create the background color of our map
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//update our camera on each frame, everytime there is a change it should be updating.
		camera.update();
		//
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		batch.end();
		//waiting until the screen is touched or the mouse is clicked
		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			//it gives us the position on where we click
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			//transform that position that we click into our map
			camera.unproject(touchPos);
			//Finally we change the position of the bucket to be centered around the touch/mouse coordinates.
			bucket.x = touchPos.x - 64 / 2;
		}
		//check if left key or right key is pressed and moves the bucket to the left or the right
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
		//makes sure that our buckets stay inside our "map"
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 800 - 64;

		//check what was the last time since the raindrop was created and make a new one if needed.
		//spawns a raindrop between 1000000000 nanoseconds.
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		//moves the raindrop from the top of the screen to the buttom.
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if(raindrop.y + 64 < 0) iter.remove();
			//if the raindrop hit the bucket it will remove it and activate drop sound
			if(raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}
		//renders the raindrops when they fall
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();
	}
	
	@Override
	//libgdx garbage collector
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
	//create a array of raindrops on the top of the screen.
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}
}
