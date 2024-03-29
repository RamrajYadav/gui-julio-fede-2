package ar.edu.itba.cg;

import com.jme.input.ChaseCamera;
import com.jme.input.InputHandler;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.system.DisplaySystem;

public class CameraManager {
	private Scene scene;
	private Dynamics dynamics;
	private InputHandler input;
	private InputHandler actualInput;
	private SceneParameters params;
	
	
	public CameraManager( Scene scene, Dynamics dynamics, InputHandler input, InputHandler actualInput, SceneParameters params ) {
		this.scene = scene;
		this.dynamics = dynamics;
		this.input = input;
		this.actualInput = actualInput;
		this.params = params;
	}
	

	private void changeInput( InputHandler newInput ) {
		input.removeFromAttachedHandlers( actualInput );
		input.addToAttachedHandlers( newInput );
		actualInput = newInput;
	}
	
	
	public void setBallCamera() {
		Camera cam = DisplaySystem.getDisplaySystem().getRenderer().getCamera();
		cam.setAxes(new Vector3f(1,1,0), new Vector3f(0,1,1), new Vector3f(0,0,1));
		ChaseCamera cameraInputHandler = new ChaseCamera( cam, dynamics.ball );
		cameraInputHandler.setActionSpeed( 5.3F );
		cameraInputHandler.setDampingK( 0 );
		cameraInputHandler.setSpringK( 0 );
        cameraInputHandler.setMaxDistance( 3 );
        cameraInputHandler.setMinDistance( 2 );
        cameraInputHandler.setTargetOffset( new Vector3f( 0, params.BALL_DIAMETER, -params.BOX_LENGTH ) );
		changeInput( cameraInputHandler );
//		cam.setUp( new Vector3f(0,1,0) );
//		cam.setLeft( new Vector3f(-1,0,0) );
//		cam.setDirection( new Vector3f(0,0,-1) );
//		cam.setLocation( new Vector3f( dynamics.ball.getLocalTranslation().x, dynamics.ball.getLocalTranslation().y + params.BALL_RADIUS, dynamics.ball.getLocalTranslation().z + params.BOX_LENGTH) );
	}
	
	
	public void setAnchorCamera() {
		Camera cam = DisplaySystem.getDisplaySystem().getRenderer().getCamera();
		ChaseCamera cameraInputHandler = new ChaseCamera( cam, dynamics.anchor );
        cameraInputHandler.setMaxDistance( 2 );
        cameraInputHandler.setMinDistance( 1 );
		changeInput( cameraInputHandler );
//		cam.setUp( new Vector3f(0,1,0) );
//		cam.setLeft( new Vector3f(-1,0,0) );
//		cam.setDirection( new Vector3f(0,0,-1) );
//		cam.setLocation( new Vector3f(0,params.BALL_DIAMETER_EXTRA,0) );
	}
	
	
}
