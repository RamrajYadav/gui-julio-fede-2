package ar.edu.itba.cg;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.input.ChaseCamera;
import com.jme.input.FirstPersonHandler;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.MusicTrackQueue;
import com.jmex.audio.MusicTrackQueue.RepeatType;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.contact.ContactCallback;
import com.jmex.physics.contact.PendingContact;
import com.jmex.physics.material.Material;
import com.jmex.physics.util.PhysicsPicker;
import com.jmex.physics.util.SimplePhysicsGame;

//	W	 Move Forward
//	A	 Strafe Left
//	S	 Move Backward
//	D	 Strafe Right
//	Up	 Look Up
//	Down	 Look Down
//	Left	 Look Left
//	Right	 Look Right
//	T	 Wireframe Mode on/off
//	P	 Pause On/Off
//	L	 Lights On/Off
//	C	 Print Camera Position
//	B	 Bounding Volumes On/Off
//	N	 Normals On/Off
//  V	 Show Physics
//	F1	 Take Screenshot

public class Bowling extends SimplePhysicsGame {
	public static String IMAGE_LOGO = "resources/logo.jpg";
	public static String TITLE = "Bowling";
	// Parameters are in centimeters!
	// Pin Parameters
	public static float PIN_HEIGHT = 0.40F;
	public static float PIN_RADIUS = 0.65F;
	public static float PIN_WEIGHT = 1.750F;
	public static int AXIS_SAMPLES = 4;		// The definition of the pin
	public static int RADIAL_SAMPLES = 10;	// The definition of the pin
	// Ball Parameters
	public static float BALL_RADIUS = 0.150F;
	public static float BALL_WEIGHT = 2.800F;
	public static int BALL_SAMPLES = 100;		// The definition of the ball
	// Gutter Parameters
	public static float GUTTER_EXTRA = 1.20F;// How much bigger or smaller than the ball (1 is the same)
	public static int GUTTER_SAMPLES = 50;	// The definition of the gutters
	// Lane Parameters
	// The middle of the foul line is at 0, BALL_RADIUS, 0
	// Ten Pin Bowling: http://en.wikipedia.org/wiki/Tenpin
	public static float LANE_WIDTH = 1.05F;
	public static float LANE_LENGTH = 18.00F;
	// Approach Parameters
	// Behind the foul line is an "approach" used to gain speed
	public static float APPROACH_LENGTH = 5.00F;
	// Room Parameters
	public static float ROOM_WIDTH = 8.00F;
	public static float ROOM_HEIGHT = 3.00F;
	// Final box
	public static float BOX_LENGTH = 1.00F;
	public static float BOX_HEIGHT = 1.00F;
	public static float BOXMACHINE_LENGTH = 2.10F;
	//Pin Positions
	//Distance between to pins (12 inches)
	public static float PIN_WIDTHDIST = 0.305F;
	//Half a distance between to pins (6 inches)
	public static float PIN_WIDTHHALFDIST = 0.152F; 
	//Depth distance between two rows of pins (10.39 inches)
	public static float PIN_HEIGHTDIST = 0.264F;
	//Initial position of the pin 1
	public static float INITIAL_POS = 0F;
	//Distance between the pin 1 and the pit relative to the end of the lane
	public static float DIST2PIT;
	// Shading
	public static float NO_SHININESS = 0.0f;
	public static float LOW_SHININESS = 5.0f;
	public static float HIGH_SHININESS = 100.0f;
	// Camera speed
	public static float CAMERA_MOVE_SPEED = 350;
	public static float CAMERA_TURN_SPEED = 1;
	public static float CAMERA_DISTANCE_MIN = 0.5F;
	public static float CAMERA_DISTANCE_MAX = 3;
	// Calculated parameters
	public static float BALL_RADIUS_EXTRA;
	public static float BALL_DIAMETER;
	public static float BALL_DIAMETER_EXTRA;
	public static float ROOM_LENGTH;
	public static float ROOM_CENTER_X;
	public static float ROOM_CENTER_Y;
	public static float ROOM_CENTER_Z;
	// Objects
	private DynamicPhysicsNode ball;
	private DynamicPhysicsNode[] pins;
	private boolean[] pinDown;
	private Text score;
	// Sounds
	private MusicTrackQueue audioQueue;
	private AudioTrack pinHit;
	// Constants
	public static ColorRGBA NO_COLOR = ColorRGBA.black;
	// Game States
	public static enum States {SHOOTING, ROWLING};
	
	
	public static void main(String [] args) throws MalformedURLException {
		Bowling app = new Bowling(); 
		app.setParameters();
		app.setConfigShowMode( ConfigShowMode.AlwaysShow, new URL("file:" + IMAGE_LOGO ) );
		app.start();
	}
	
	
	private void setParameters() {
		 Properties props = new Properties();
            try {
            	props.load(new FileInputStream("resources/configFiles/test.properties"));
                for(Enumeration properties=props.propertyNames(); properties.hasMoreElements();) {
	            	try {
	            		String property = (String)properties.nextElement();
						Field field = Bowling.class.getField(property);
						Type type = field.getType();
						if (type.equals(int.class)) {
							field.set(null,Integer.parseInt(props.getProperty(property)));
						}else if(type.equals(float.class)){
							field.set(null,Float.parseFloat(props.getProperty(property)));
						}else{
							field.set(null,props.getProperty(property));
						}
	            	} catch (SecurityException e) {
						System.err.println("Parametro inexistente: " + e.getMessage());
					} catch (NoSuchFieldException e) {
						System.err.println("Parametro inexistente: " + e.getMessage());
					} catch (IllegalArgumentException e) {
						System.err.println("Parametro inexistente: " + e.getMessage());
					} catch (IllegalAccessException e) {
						System.err.println("Parametro inexistente: " + e.getMessage());
					}
	            }
            }catch(IOException e) {
            	System.err.println("Properties file could not be loaded");
            }
    		BALL_RADIUS_EXTRA = BALL_RADIUS * GUTTER_EXTRA;
    		BALL_DIAMETER = BALL_RADIUS * 2;
    		BALL_DIAMETER_EXTRA = BALL_RADIUS_EXTRA * 2;
    		ROOM_LENGTH = LANE_LENGTH + APPROACH_LENGTH + BOX_LENGTH;
    		ROOM_CENTER_X = 0;
    		ROOM_CENTER_Y = ROOM_HEIGHT / 2;
    		ROOM_CENTER_Z = APPROACH_LENGTH / 2 - BOX_LENGTH / 2 - LANE_LENGTH / 2;
    		DIST2PIT = -LANE_LENGTH + 0.868F ;
	}
	
	
	@Override
	protected void simpleInitGame() {
		// Display
		this.createDisplay();
		// Physics
		this.createPhysics();
		// Audio
		this.createAudio();
		// Room
		this.createRoom();
		// Box
		this.createBox();
		// Lane
		this.createLane();
		// Approach
		this.createApproach();
		// Ball
		this.createBall();
		// Pins
		this.createPins();
		// Gutters
		this.createGutters();
		// Lights
		this.createIlumination();
		// Camera
		this.createCamera();
		// Create controls
		this.createControls();
		// Update
		rootNode.updateRenderState();
	}
	
	
	@Override
	protected void simpleUpdate() {
		if ( KeyInput.get().isKeyDown(KeyInput.KEY_SPACE)) {
			this.resetBall();
			this.resetPins();
		}
		if ( KeyInput.get().isKeyDown(KeyInput.KEY_RETURN)) {
			this.resetBall();
			this.removePins();
		}
		if( KeyInput.get().isKeyDown(KeyInput.KEY_PGUP) && ball.getLocalTranslation().z > -1 ) {
			Vector3f speed = new Vector3f(0,0,-20);
			this.ball.addForce( speed );
		}
		this.score.print(" Pins down: " + this.numberOfPins() );
	}
	
	
	private void createDisplay() {
		display.getRenderer().setBackgroundColor( ColorRGBA.black );
		display.getRenderer().getCamera().setFrustumFar( ROOM_LENGTH * 1.1f);
		display.getRenderer().getCamera().update();
		display.setTitle( TITLE );
		score = Text.createDefaultTextLabel( "score", "" );
		score.setLocalTranslation( 0, 20, 0 );
		statNode.attachChild( score );
	}
	
	
	private void createPhysics() {
		getPhysicsSpace().setAutoRestThreshold( 0.0f );
        setPhysicsSpeed( 4 );
		// getPhysicsSpace().setWorldBounds( new Vector3f(ROOM_CENTER_X - ROOM_WIDTH, ROOM_CENTER_Y - ROOM_HEIGHT, ROOM_CENTER_Z - ROOM_LENGTH), new Vector3f(ROOM_CENTER_X + ROOM_WIDTH, ROOM_CENTER_Y + ROOM_HEIGHT, ROOM_CENTER_Z + ROOM_LENGTH) );
		// getPhysicsSpace().setWorldBounds( new Vector3f(-9999,-9999,-9999), new Vector3f(9999,9999,9999) );
		// getPhysicsSpace().setDirectionalGravity( new Vector3f(0,-9.81F,0) );
        ContactCallback myCallBack = new ContactCallback() {
			public boolean adjustContact( PendingContact c ) {
				String name1 = c.getNode1().getName();
				String name2 = c.getNode2().getName();
				if( name1 == null || name2 == null ) {
					return false;
				}
				if( name1.startsWith( "pin" ) && name2.startsWith( "pin" ) ) {
                    playSound( pinHit );
                }else if( name1.startsWith( "pin" ) && name2.startsWith( "ball" ) || name2.startsWith( "pin" ) && name1.startsWith( "ball" )) {
                    System.out.println( "Pin Ball collition!");
                }
                // everything normal, continue with next callback
                return false;
            }
			
        };
        getPhysicsSpace().getContactCallbacks().add( myCallBack );
	}
	
	
	private void createAudio() {
		audioQueue = AudioSystem.getSystem().getMusicQueue();
		audioQueue.setCrossfadeinTime(0);
		audioQueue.setRepeatType(RepeatType.NONE);
		pinHit = getAudioTrack( "resources/Sounds/pinHit.ogg" );
	}
	
	
	private void createCamera() {
		//cam.setLocation( new Vector3f(0,80,30) );
		//ChaseCamera chaser = new ChaseCamera( cam, ball);
		//input = new FirstPersonHandler( cam, CAMERA_MOVE_SPEED, CAMERA_TURN_SPEED );
		// Simple chase camera
        input.removeFromAttachedHandlers( cameraInputHandler );
        cameraInputHandler = new ChaseCamera( cam, ball );
        cameraInputHandler.setActionSpeed( 0.3F );
        ((ChaseCamera)cameraInputHandler).setMaxDistance(CAMERA_DISTANCE_MAX);
        ((ChaseCamera)cameraInputHandler).setMinDistance(CAMERA_DISTANCE_MIN);
        input.addToAttachedHandlers( cameraInputHandler );
	}
	
	
	private void createControls() {
		//new PhysicsPicker( input, rootNode, getPhysicsSpace() );
        //MouseInput.get().setCursorVisible( true );
	}
	
	
	private void createRoom() {
		Node room = new Node("room");
		// Top and bottom
		Quad wallDownVisual = new Quad("wall_down", ROOM_WIDTH, ROOM_LENGTH );
		Quad wallUpVisual =   new Quad("wall_up",   ROOM_WIDTH, ROOM_LENGTH );
		wallDownVisual.setModelBound( new BoundingBox() ); 
		wallDownVisual.updateModelBound();
		wallUpVisual.setModelBound( new BoundingBox() ); 
		wallUpVisual.updateModelBound();
		setColor( wallDownVisual, ColorRGBA.white, NO_SHININESS, NO_COLOR );
		setColor( wallUpVisual,   ColorRGBA.white, NO_SHININESS, NO_COLOR );
		setTexture( wallDownVisual, "resources/textures/wall.jpg" );
		setTexture( wallUpVisual, "resources/textures/wall.jpg" );
		StaticPhysicsNode wallDown = getPhysicsSpace().createStaticNode();
		StaticPhysicsNode wallUp = getPhysicsSpace().createStaticNode();
		wallDown.setName( "wall_down" );
		wallUp.setName( "wall_up" );
		wallDown.attachChild( wallDownVisual );
		wallUp.attachChild( wallUpVisual );
		wallDown.setMaterial( Material.CONCRETE );
		wallUp.setMaterial( Material.CONCRETE );
		wallDown.setLocalRotation( new Quaternion( new float[]{ (float)Math.PI/2, 0, 0 } ) );
		wallUp.setLocalRotation(   new Quaternion( new float[]{ (float)Math.PI/2, 0, 0 } ) );
		wallDown.setLocalTranslation( 0, 0, ROOM_CENTER_Z );
		wallUp.setLocalTranslation(   0, ROOM_HEIGHT, ROOM_CENTER_Z );
		wallDown.generatePhysicsGeometry();
		wallUp.generatePhysicsGeometry();
		// Left and right
		Quad wallLeftVisual =  new Quad("wall_left",  ROOM_LENGTH, ROOM_HEIGHT);
		Quad wallRightVisual = new Quad("wall_right", ROOM_LENGTH, ROOM_HEIGHT);
		wallLeftVisual.setModelBound( new BoundingBox() ); 
		wallLeftVisual.updateModelBound();
		wallRightVisual.setModelBound( new BoundingBox() ); 
		wallRightVisual.updateModelBound();
		setColor( wallLeftVisual,  ColorRGBA.white, NO_SHININESS, NO_COLOR );
		setColor( wallRightVisual, ColorRGBA.white, NO_SHININESS, NO_COLOR );
		setTexture( wallLeftVisual, "resources/textures/wall.jpg" );
		setTexture( wallRightVisual, "resources/textures/wall.jpg" );
		StaticPhysicsNode wallLeft = getPhysicsSpace().createStaticNode();
		StaticPhysicsNode wallRight = getPhysicsSpace().createStaticNode();
		wallLeft.setName( "wall_left" );
		wallRight.setName( "wall_right" );
		wallLeft.attachChild( wallLeftVisual );
		wallRight.attachChild( wallRightVisual );
		wallLeft.setMaterial( Material.CONCRETE );
		wallRight.setMaterial( Material.CONCRETE );
		wallLeft.setLocalRotation(  new Quaternion( new float[]{ 0, (float)Math.PI/2, 0 } ) );
		wallRight.setLocalRotation( new Quaternion( new float[]{ 0, (float)Math.PI/2, 0 } ) );
		wallLeft.setLocalTranslation(  -ROOM_WIDTH / 2, ROOM_CENTER_Y, ROOM_CENTER_Z );
		wallRight.setLocalTranslation(  ROOM_WIDTH / 2, ROOM_CENTER_Y, ROOM_CENTER_Z );
		wallRight.generatePhysicsGeometry();
		wallLeft.generatePhysicsGeometry();
		// Back
		Quad wallBackVisual =  new Quad("wall_back", ROOM_HEIGHT, ROOM_WIDTH);
		wallBackVisual.setModelBound( new BoundingBox() ); 
		wallBackVisual.updateModelBound();
		setColor( wallBackVisual, ColorRGBA.white, NO_SHININESS, NO_COLOR );
		setTexture( wallBackVisual, "resources/textures/wall.jpg" );
		StaticPhysicsNode wallBack = getPhysicsSpace().createStaticNode();
		wallBack.setName( "wall_back" );
		wallBack.attachChild( wallBackVisual );
		wallBack.setMaterial( Material.CONCRETE );
		wallBack.setLocalRotation(  new Quaternion( new float[]{ 0, 0, (float)Math.PI/2 } ) );
		wallBack.setLocalTranslation(  0, ROOM_CENTER_Y, APPROACH_LENGTH );
		wallBack.generatePhysicsGeometry();
		// Front
		Quad wallFrontVisual =  new Quad("wall_front", ROOM_HEIGHT, ROOM_WIDTH);
		wallFrontVisual.setModelBound( new BoundingBox() ); 
		wallFrontVisual.updateModelBound();
		setColor( wallFrontVisual, ColorRGBA.white, NO_SHININESS, NO_COLOR );
		setTexture( wallFrontVisual, "resources/textures/wall.jpg" );
		StaticPhysicsNode wallFront = getPhysicsSpace().createStaticNode();
		wallFront.setName( "wall_front" );
		wallFront.attachChild( wallFrontVisual );
		wallFront.setMaterial( Material.CONCRETE );
		wallFront.setLocalRotation(  new Quaternion( new float[]{ 0, 0, (float)Math.PI/2 } ) );
		wallFront.setLocalTranslation(  0, ROOM_CENTER_Y, -(LANE_LENGTH + BOX_LENGTH) );
		wallFront.generatePhysicsGeometry();
		// Attach
		room.attachChild( wallDown );
		room.attachChild( wallUp );
		room.attachChild( wallLeft );
		room.attachChild( wallRight );
		room.attachChild( wallBack );
		room.attachChild( wallFront );
		room.setModelBound( new BoundingBox() ); 
		room.updateModelBound();
		rootNode.attachChild( room );	
	}
	
	
	private void createBox() {
		// TODO: FIX THE BOX
		Node box = new Node("box");
		// Top
		Quad boxTopVisual = new Quad("box_top", LANE_WIDTH + BALL_DIAMETER_EXTRA * 2, BOX_LENGTH + BOXMACHINE_LENGTH );
		boxTopVisual.setModelBound( new BoundingBox() ); 
		boxTopVisual.updateModelBound();
		setColor( boxTopVisual, ColorRGBA.darkGray, NO_SHININESS, NO_COLOR );
		StaticPhysicsNode boxTop = getPhysicsSpace().createStaticNode();
		boxTop.setName( "box_top" );
		boxTop.attachChild( boxTopVisual );
		boxTop.setMaterial( Material.CONCRETE );
		boxTop.setLocalRotation(  new Quaternion( new float[]{ (float)Math.PI/2, 0, 0 } ) );
		boxTop.setLocalTranslation(  0, BALL_RADIUS_EXTRA + BOX_HEIGHT, -(LANE_LENGTH) );
		boxTop.generatePhysicsGeometry();
		// Left and right
		Quad boxLeftVisual =  new Quad("box_left",  BOX_LENGTH + BOXMACHINE_LENGTH, BALL_RADIUS_EXTRA + BOX_HEIGHT);
		Quad boxRightVisual = new Quad("box_right", BOX_LENGTH + BOXMACHINE_LENGTH, BALL_RADIUS_EXTRA + BOX_HEIGHT);
		boxLeftVisual.setModelBound( new BoundingBox() ); 
		boxLeftVisual.updateModelBound();
		boxRightVisual.setModelBound( new BoundingBox() ); 
		boxRightVisual.updateModelBound();
		setColor( boxLeftVisual,  ColorRGBA.darkGray, NO_SHININESS, NO_COLOR );
		setColor( boxRightVisual, ColorRGBA.darkGray, NO_SHININESS, NO_COLOR );
		StaticPhysicsNode boxLeft = getPhysicsSpace().createStaticNode();
		StaticPhysicsNode boxRight = getPhysicsSpace().createStaticNode();
		boxLeft.setName( "box_left" );
		boxRight.setName( "box_right" );
		boxLeft.attachChild( boxLeftVisual );
		boxRight.attachChild( boxRightVisual );
		boxLeft.setMaterial( Material.CONCRETE );
		boxRight.setMaterial( Material.CONCRETE );
		boxLeft.setLocalRotation(  new Quaternion( new float[]{ 0, (float)Math.PI/2, 0 } ) );
		boxRight.setLocalRotation( new Quaternion( new float[]{ 0, (float)Math.PI/2, 0 } ) );
		boxLeft.setLocalTranslation(  -(LANE_WIDTH / 2 + BALL_DIAMETER_EXTRA), (BALL_RADIUS_EXTRA + BOX_HEIGHT)/2, -(LANE_LENGTH ) );
		boxRight.setLocalTranslation(   LANE_WIDTH / 2 + BALL_DIAMETER_EXTRA,  (BALL_RADIUS_EXTRA + BOX_HEIGHT)/2, -(LANE_LENGTH ) );
		boxRight.generatePhysicsGeometry();
		boxLeft.generatePhysicsGeometry();
		// Attach
		box.attachChild( boxTop );
		box.attachChild( boxLeft );
		box.attachChild( boxRight );
		box.setModelBound( new BoundingBox() ); 
		box.updateModelBound();
		rootNode.attachChild( box );	
	}
	
	
	private void createLane() {
		Box laneVisual = new Box("lane", new Vector3f(0,0,0), LANE_WIDTH / 2, BALL_RADIUS_EXTRA / 2, LANE_LENGTH / 2 );
		laneVisual.setModelBound( new BoundingBox() ); 
		laneVisual.updateModelBound();
		setColor( laneVisual, ColorRGBA.white, NO_SHININESS, NO_COLOR );
		setTexture( laneVisual, "resources/textures/wood.jpg" );
		StaticPhysicsNode lane = getPhysicsSpace().createStaticNode();
		lane.setName( "lane" );
		lane.setMaterial( Material.WOOD );
		lane.attachChild( laneVisual );
		lane.setLocalTranslation( new Vector3f(0, BALL_RADIUS_EXTRA / 2, -LANE_LENGTH / 2) );
		lane.generatePhysicsGeometry();
		rootNode.attachChild( lane );
	}
	
	
	private void createApproach() {
		Box approachVisual = new Box( "approach", new Vector3f(0,0,0), LANE_WIDTH/2 + BALL_DIAMETER_EXTRA, BALL_RADIUS_EXTRA / 2, APPROACH_LENGTH / 2 );
		approachVisual.setModelBound( new BoundingBox() ); 
		approachVisual.updateModelBound();
		setColor( approachVisual, ColorRGBA.white, NO_SHININESS, NO_COLOR );
		setTexture( approachVisual, "resources/textures/wood.jpg" );
		StaticPhysicsNode approach = getPhysicsSpace().createStaticNode();
		approach.setName( "approach" );
		approach.setMaterial( Material.WOOD );
		approach.attachChild( approachVisual );
		approach.setLocalTranslation( new Vector3f(0, BALL_RADIUS_EXTRA / 2, APPROACH_LENGTH / 2) );
		approach.generatePhysicsGeometry();
		rootNode.attachChild( approach );
	}
	
	
	private void createBall() {
		Sphere ballVisual = new Sphere("ball", new Vector3f(0, 0, 0), BALL_SAMPLES, BALL_SAMPLES, BALL_RADIUS);
		ballVisual.setModelBound( new BoundingSphere() ); 
		ballVisual.updateModelBound();
		setColor( ballVisual, ColorRGBA.green, HIGH_SHININESS, ColorRGBA.white );
		setTexture( ballVisual, "resources/textures/marble.jpg" );
		this.ball = getPhysicsSpace().createDynamicNode();
		ball.setName( "ball" );
		this.ball.setMaterial( Material.GRANITE );
		this.ball.attachChild( ballVisual );
		this.ball.generatePhysicsGeometry(); 
		this.ball.setMass(BALL_WEIGHT);
		rootNode.attachChild( this.ball );
		resetBall();
	}
	
	
	private void resetBall() {
		ball.setActive(true);
		ball.clearDynamics();
		ball.setLocalTranslation( new Vector3f(0, BALL_RADIUS_EXTRA + BALL_RADIUS, APPROACH_LENGTH / 2) );
	}
	
	
	private void createPins() {
		this.pins = new DynamicPhysicsNode[10];
		for( int i = 0; i < 10; i++ ){
			Cylinder pinVisual = new Cylinder("pin_"+ i,AXIS_SAMPLES,RADIAL_SAMPLES,PIN_RADIUS,PIN_HEIGHT,true);
			pinVisual.setModelBound( new BoundingBox() );
			pinVisual.updateModelBound();
			pinVisual.lockMeshes();
			setColor( pinVisual, ColorRGBA.red, HIGH_SHININESS, ColorRGBA.white );
			pins[i] = getPhysicsSpace().createDynamicNode();
			pins[i].setName( "pin_" + i);
			//pins[i].setCenterOfMass( new Vector3f(0,0,PIN_HEIGHT*1/8) );
			pins[i].setMaterial( Material.GRANITE );
			pins[i].attachChild(pinVisual);
			pins[i].generatePhysicsGeometry();
			pins[i].setMass(PIN_WEIGHT);
			rootNode.attachChild( pins[i] );
		}
		resetPins();
	}
	
	
	private Vector3f getPinPosition(int i) {
		switch( i ) {
			case 0:
				return new Vector3f(INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT));
			case 1:
				return new Vector3f(-PIN_WIDTHHALFDIST + INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)- PIN_HEIGHTDIST);
			case 2:
				return new Vector3f(PIN_WIDTHHALFDIST + INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)- PIN_HEIGHTDIST);
			case 3:
				return new Vector3f(-PIN_WIDTHDIST + INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)- (2 * PIN_HEIGHTDIST));
			case 4:
				return new Vector3f(INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)- (2 * PIN_HEIGHTDIST));
			case 5:
				return new Vector3f(PIN_WIDTHDIST + INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)- (2 * PIN_HEIGHTDIST));
			case 6:
				return new Vector3f(-(PIN_WIDTHDIST + PIN_WIDTHHALFDIST)+ INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)-(3 * PIN_HEIGHTDIST));
			case 7:
				return new Vector3f(-PIN_WIDTHHALFDIST + INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)-(3 * PIN_HEIGHTDIST));
			case 8:
				return new Vector3f(PIN_WIDTHHALFDIST + INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)-(3 * PIN_HEIGHTDIST));
			case 9:
				return new Vector3f((PIN_WIDTHDIST+PIN_WIDTHHALFDIST)+ INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT)-(3 * PIN_HEIGHTDIST));
			default:
				return new Vector3f(INITIAL_POS,BALL_RADIUS_EXTRA + (PIN_HEIGHT/2), (DIST2PIT));
		}
	}
	
	
	private void resetPins() {
		for( int i = 0; i < 10; i++ ){
			pins[i].rest();
			pins[i].clearDynamics();
			pins[i].setLocalRotation(new Quaternion( new float[]{(float)Math.PI/2,0,0} ));
			pins[i].setLocalTranslation( getPinPosition(i) );
		}
	}
	
	private void removePins() {
		for( int i = 0; i < 10; i++ ){
			pins[i].rest();
			pins[i].clearDynamics();
			if(isPinDown(pins[i]))
			{
				pins[i].setLocalRotation(new Quaternion( new float[]{(float)Math.PI/2,0,0} ));
				pins[i].setLocalTranslation( new Vector3f(9999,9999,9999) );
				
			}
			
		}
	}
	//Calculates if the coordinates are inside the box
	private boolean outOfBounds(float x, float z){
			if((x > -LANE_WIDTH/2 && x < LANE_WIDTH/2)&&(z > -LANE_LENGTH && z <(-LANE_LENGTH + BOXMACHINE_LENGTH)))
				return true;
			return false;
	}
	
	private boolean isPinDown(DynamicPhysicsNode pin){
		
		Double tippingCouple = Math.PI*7/36;
		
		if(!this.outOfBounds(pin.getLocalTranslation().x,pin.getLocalTranslation().z)){
			return true;
		}
		else if( (Math.abs( pin.getLocalRotation().toAngles(null)[0] - Math.PI/2 ) > tippingCouple) || 
        	(Math.abs( pin.getLocalRotation().toAngles(null)[2] - 0 ) > tippingCouple)) 
        {
            return true;           
        }
		return false;
		
	}
	
	private int numberOfPins() {
		int count = 0;
		
		for( int i = 0; i < 10; i++) {
			if(isPinDown(pins[i]))
               count++;
        }
		return count;
	}
	
	
	private void createGutters() {
		float circumference = 2.0F * (float)Math.PI * BALL_RADIUS_EXTRA;
		// Node gutterLeft =  getPhysicsSpace().createStaticNode(); // new Node("gutter_left");
		// Node gutterRight = getPhysicsSpace().createStaticNode(); // new Node("gutter_right");
		Quad gutterBorderLeftVisual  = new Quad("gutter_border_left",  BALL_RADIUS_EXTRA, LANE_LENGTH);
		Quad gutterBorderRightVisual = new Quad("gutter_border_right", BALL_RADIUS_EXTRA, LANE_LENGTH);
		gutterBorderLeftVisual.setModelBound( new BoundingBox() ); 
		gutterBorderLeftVisual.updateModelBound();
		gutterBorderRightVisual.setModelBound( new BoundingBox() ); 
		gutterBorderRightVisual.updateModelBound();
		setColor( gutterBorderLeftVisual, ColorRGBA.gray, LOW_SHININESS, NO_COLOR );
		setColor( gutterBorderRightVisual,   ColorRGBA.gray, LOW_SHININESS, NO_COLOR );
		setTexture( gutterBorderLeftVisual, "resources/textures/metal.jpg" );
		setTexture( gutterBorderRightVisual, "resources/textures/metal.jpg" );
		StaticPhysicsNode gutterBorderLeft = getPhysicsSpace().createStaticNode();
		StaticPhysicsNode gutterBorderRight = getPhysicsSpace().createStaticNode();
		gutterBorderLeft.setName( "gutter_border_left" );
		gutterBorderRight.setName( "gutter_border_right" );
		gutterBorderLeft.setMaterial( Material.IRON );
		gutterBorderRight.setMaterial( Material.IRON );
		gutterBorderLeft.attachChild( gutterBorderLeftVisual );
		gutterBorderRight.attachChild( gutterBorderRightVisual );
		gutterBorderLeft.setLocalRotation(  new Quaternion( new float[]{ (float)Math.PI/2, 0, -(float)Math.PI/2 } ) );
		gutterBorderRight.setLocalRotation( new Quaternion( new float[]{ (float)Math.PI/2, 0, -(float)Math.PI/2 } ) );
		gutterBorderLeft.setLocalTranslation( -(LANE_WIDTH/2 + BALL_DIAMETER_EXTRA), BALL_RADIUS_EXTRA / 2, -(LANE_LENGTH / 2) );
		gutterBorderRight.setLocalTranslation( LANE_WIDTH/2 + BALL_DIAMETER_EXTRA  , BALL_RADIUS_EXTRA / 2, -(LANE_LENGTH / 2) );
		gutterBorderLeft.generatePhysicsGeometry();
		gutterBorderRight.generatePhysicsGeometry();
		rootNode.attachChild( gutterBorderLeft );
		rootNode.attachChild( gutterBorderRight );
		for( int i = 1; i < GUTTER_SAMPLES; i++ ) {
			Quad leftVisual =  new Quad( "gutter_left_"  + String.valueOf(i), circumference / GUTTER_SAMPLES, LANE_LENGTH);
			Quad rightVisual = new Quad( "gutter_right_" + String.valueOf(i), circumference / GUTTER_SAMPLES, LANE_LENGTH);
			leftVisual.setModelBound( new BoundingBox() ); 
			leftVisual.updateModelBound();
			rightVisual.setModelBound( new BoundingBox() ); 
			rightVisual.updateModelBound();
			setColor( leftVisual, ColorRGBA.gray, LOW_SHININESS, NO_COLOR );
			setColor( rightVisual,   ColorRGBA.gray, LOW_SHININESS, NO_COLOR );
			setTexture( leftVisual,  "resources/textures/metal.jpg" );
			setTexture( rightVisual, "resources/textures/metal.jpg" );
			StaticPhysicsNode left = getPhysicsSpace().createStaticNode();
			StaticPhysicsNode right = getPhysicsSpace().createStaticNode();
			left.setName( "gutter_left_" + String.valueOf(i) );
			right.setName( "gutter_right_" + String.valueOf(i) );
			left.setMaterial( Material.IRON );
			right.setMaterial( Material.IRON );
			left.attachChild( leftVisual );
			right.attachChild( rightVisual );
			float rotation =  -(float)Math.PI/2 + (float)Math.PI / GUTTER_SAMPLES * i; 
			left.setLocalRotation(  new Quaternion( new float[]{ (float)Math.PI/2, 0, rotation } ) );
			right.setLocalRotation( new Quaternion( new float[]{ (float)Math.PI/2, 0, rotation } ) );
			float angle = (float)Math.PI + (float)Math.PI / GUTTER_SAMPLES * i;
			left.setLocalTranslation( -(LANE_WIDTH/2 + BALL_RADIUS_EXTRA) + BALL_RADIUS_EXTRA * (float)Math.cos( angle ), BALL_RADIUS_EXTRA + BALL_RADIUS_EXTRA * (float)Math.sin( angle ), -(LANE_LENGTH / 2) );
			right.setLocalTranslation( LANE_WIDTH/2 + BALL_RADIUS_EXTRA + BALL_RADIUS_EXTRA * (float)Math.cos( angle ), BALL_RADIUS_EXTRA + BALL_RADIUS_EXTRA * (float)Math.sin( angle ), -(LANE_LENGTH / 2) );
			left.generatePhysicsGeometry();
			right.generatePhysicsGeometry();
			rootNode.attachChild( left );
			rootNode.attachChild( right );
		}	
	}
	
	
	private void createIlumination() {
		((PointLight)lightState.get(0)).setLocation(new Vector3f(0, ROOM_HEIGHT * 0.9F, 0));
		lightState.setTwoSidedLighting(true);
		
//		// Create a point light
//		PointLight l=new PointLight();
//		// Give it a location
//		l.setLocation(new Vector3f(0,25,15));
//		// Make it a red light
//		l.setDiffuse(ColorRGBA.red);
//		// Create a LightState to put my light in
//		LightState ls=display.getRenderer().createLightState();
//		// Attach the light
//		ls.attach(l);
//		lightState.detachAll();
		
	} 

// Clear Light state
//        lightState.detachAll();
//        lightState.setEnabled( true );
//        lightState.setGlobalAmbient( ColorRGBA.white.clone() );
        // Light
//		PointLight light = new PointLight();
//        light.setAmbient( ColorRGBA.white.clone() );
//        light.setDiffuse( ColorRGBA.white.clone() );
//        light.setSpecular( ColorRGBA.white.clone() );
//        light.setAttenuate( true );
//        light.setLocation( new Vector3f( 0, ROOM_HEIGHT * 0.9F, 0 ) );
//        light.setEnabled( true );
//        lightState.attach( light );

	
	private void setColor( Spatial spatial, ColorRGBA diffuseColor, float shininess, ColorRGBA specularColor ) {
        final MaterialState materialState = display.getRenderer().createMaterialState();
        materialState.setDiffuse( diffuseColor );
        materialState.setSpecular( specularColor );
        materialState.setShininess( shininess );
        materialState.setEmissive( NO_COLOR );
        materialState.setEnabled( true );
        spatial.setRenderState( materialState );
    }
	
	
	private void setTexture( Spatial spatial, String image ) {
		TextureState textureState = display.getRenderer().createTextureState();
		Texture texture = null;
		try {
			texture = TextureManager.loadTexture(
				new URL( "file:" + image ),
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear 
			);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		texture.setWrap( WrapMode.Repeat );
		textureState.setTexture( texture );
	    spatial.setRenderState(textureState);
	}

	
	public AudioTrack getAudioTrack(String file) {
		AudioTrack track = null;
		try {
			track = AudioSystem.getSystem().createAudioTrack( new URL( "file:" + file ), false);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		track.setLooping( false );
		track.setVolume( 1.0f );
		return track;
	}
	
	
	public void playSound(AudioTrack track) {
		if( !track.isPlaying() ) {
			audioQueue.addTrack(track);
			audioQueue.play();
		}
		AudioSystem.getSystem().update();
		AudioSystem.getSystem().fadeOutAndClear(1.5f);
	}
	
	
}