package com.mygdx.threedtests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ThreeDTest extends ApplicationAdapter {
	public PerspectiveCamera cam;
	public LightsCameraActionInputController inputController;

	public ModelBatch modelBatch;
	public Model modelBox;
	public ModelInstance instanceBox;
	public Model modelPlane;
	public ModelInstance instancePlane;
	public Array<ModelInstance> renderables = new Array<>();

	public Environment environment;
	public DirectionalShadowLight dLight;
	public ModelBatch shadowBatch;
	
	@Override
	public void create () {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		// Create Camera
		cam = new PerspectiveCamera(67, width, height);
		cam.position.set(8.4f, 4f, 1.1f);
		cam.lookAt(2.8f, 0f, -0.8f);
		cam.near = 1f;
		cam.far = 50f;
		cam.update();

		modelBatch = new ModelBatch();

		// Create cube (for testing only)
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBox = modelBuilder.createBox(2f, 2f, 2f,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				Usage.Position | Usage.Normal);
		instanceBox = new ModelInstance(modelBox, 2.8f, 0f, -0.8f);
		instanceBox.transform.rotateTowardDirection(new Vector3(cam.direction.x, 0, cam.direction.z), Vector3.Y);

		modelBuilder.begin();
		MeshPartBuilder mpb = modelBuilder.part("parts", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.ColorUnpacked,
				new Material(ColorAttribute.createDiffuse(Color.WHITE)));
		mpb.setColor(1f, 1f, 1f, 1f);
		mpb.box(0, -1.5f, 0, 10, 1, 10);
		mpb.setColor(1f, 0f, 1f, 1f);
		mpb.sphere(2f, 2f, 2f, 100, 100);
		modelPlane = modelBuilder.end();
		instancePlane = new ModelInstance(modelPlane);

		renderables.add(instancePlane, instanceBox);

		// Create environment lighting
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.6f, 0.4f, 1f));
		environment.add((dLight = new DirectionalShadowLight(1024, 1024, 30f, 30f, 1f, 100f))
				.set(1f, 1f, 1f, -3f, -1.6f, -0.4f));
		environment.shadowMap = dLight;
		shadowBatch = new ModelBatch(new DepthShaderProvider());

		// Input controller for mouse and keyboard events
		inputController = new LightsCameraActionInputController(cam, dLight, environment, instanceBox);
		inputController.target = new Vector3(2.8f, 0f, -0.8f);
		Gdx.input.setInputProcessor(inputController);
	}

	@Override
	public void render () {
		inputController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		dLight.begin(Vector3.Zero, cam.direction);
			shadowBatch.begin(dLight.getCamera());
				shadowBatch.render(renderables);
			shadowBatch.end();
		dLight.end();

		modelBatch.begin(cam);
		modelBatch.render(renderables, environment);
		modelBatch.end();
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		modelBox.dispose();
		modelPlane.dispose();
	}
}
