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
	public Model axesModel;
	public ModelInstance axesInstance;
	public Array<ModelInstance> renderables = new Array<>();

	public Environment environment;
	public DirectionalShadowLight dLight;
	public ModelBatch shadowBatch;
	public DayNightCycle dayNightCycle;

	private final Vector3 tmpV1 = new Vector3();
	
	@Override
	public void create () {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		// Create Camera
		cam = new PerspectiveCamera(67, width, height);
		cam.position.set(-5f, 5f, 7f);
		cam.lookAt(-5f, 0f, -1.5f);
		cam.near = 1f;
		cam.far = 50f;
		cam.update();

		modelBatch = new ModelBatch();

		// Create cube (for testing only)
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBox = modelBuilder.createBox(2f, 2f, 2f,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				Usage.Position | Usage.Normal);
		instanceBox = new ModelInstance(modelBox, -5f, 0f, -1.5f);
		instanceBox.transform.rotateTowardDirection(new Vector3(cam.direction.x, 0, cam.direction.z), Vector3.Y);

		modelBuilder.begin();
		MeshPartBuilder mpb = modelBuilder.part("parts", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.ColorUnpacked,
				new Material(ColorAttribute.createDiffuse(Color.WHITE)));
		mpb.setColor(1f, 1f, 1f, 1f);
		mpb.box(0, -1.5f, 0, 100, 1, 100);
		mpb.setColor(1f, 0f, 1f, 1f);
		mpb.sphere(2f, 2f, 2f, 100, 100);
		modelPlane = modelBuilder.end();
		instancePlane = new ModelInstance(modelPlane);

		createAxes();

		renderables.add(instancePlane, instanceBox, axesInstance);

		// Create environment lighting
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
		environment.add((dLight = new DirectionalShadowLight(4096, 4096, 100f, 100f, 1f, 200f))
				.set(1f, 1f, 1f, new Vector3(17f, -1f, 3f).nor()));
		environment.shadowMap = dLight;
		DirectionalShadowLight moon = (DirectionalShadowLight) new DirectionalShadowLight(4096, 4096, 100f, 100f, 1f, 200f)
				.set(1f, 1f, 1f, new Vector3(17f, -1f, 3f).nor());

		shadowBatch = new ModelBatch(new DepthShaderProvider());

		// Input controller for mouse and keyboard events
		inputController = new LightsCameraActionInputController(cam, dLight, environment, instanceBox);
		inputController.target = new Vector3(-5f, 0f, -1.5f);
		Gdx.input.setInputProcessor(inputController);

		dayNightCycle = new DayNightCycle(environment, dLight, moon, 40);
	}

	@Override
	public void render () {
		inputController.update();
		cam.update();
		dayNightCycle.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		dLight.begin(tmpV1.set(cam.position.x, 0, cam.position.z), cam.direction);
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
		dLight.dispose();
		axesModel.dispose();
		axesModel = null;
	}

	final float GRID_MIN = -50;
	final float GRID_MAX = 50;
	final float GRID_STEP = 1f;
	private void createAxes () {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(Color.LIGHT_GRAY);
		for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
			builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
			builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
		}
		builder = modelBuilder.part("axes", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(Color.RED);
		builder.line(0, 0, 0, 100, 0, 0);
		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, 0, 100, 0);
		builder.setColor(Color.BLUE);
		builder.line(0, 0, 0, 0, 0, 100);
		axesModel = modelBuilder.end();
		axesInstance = new ModelInstance(axesModel);
	}
}
