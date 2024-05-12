package com.mygdx.threedtests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.threedtests.shadows.system.ShadowSystem;
import com.mygdx.threedtests.shadows.system.classical.ClassicalShadowSystem;
import com.mygdx.threedtests.shadows.utils.AABBNearFarAnalyzer;
import com.mygdx.threedtests.shadows.utils.BoundingSphereDirectionalAnalyzer;
import com.mygdx.threedtests.shadows.utils.FixedShadowMapAllocator;
import com.mygdx.threedtests.shadows.utils.FrustumLightFilter;

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
	public ShadowSystem system;
	public DirectionalLight sun, moon;
	public Array<ModelBatch> passBatches;
	
	@Override
	public void create () {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		// Create Camera
		cam = new PerspectiveCamera(67, width, height);
		cam.position.set(8.4f, 5f, 1.1f);
		cam.lookAt(2.8f, 0f, -0.8f);
		cam.near = 1f;
		cam.far = 50f;
		cam.update();

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
		mpb.box(0, -1.5f, 0, 50, 1, 50);
		mpb.setColor(1f, 0f, 1f, 1f);
		mpb.sphere(2f, 2f, 2f, 100, 100);
		modelPlane = modelBuilder.end();
		instancePlane = new ModelInstance(modelPlane);

		renderables.add(instancePlane, instanceBox);

		// Create environment lighting
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.6f, 0.4f, 1f));
		environment.add(sun = new DirectionalLight().set(1f, 1f, 1f, new Vector3(-3f, -1.6f, -0.4f).nor()));
		environment.add(moon = new DirectionalLight().set(.6f, .6f, .6f, new Vector3(0.4f, -1.6f, -3f).nor()));
		moon.direction.y = -1.6f;

		/* The ShadowSystem has been lifted from https://github.com/libgdx/libgdx/tree/master/tests/gdx-tests/src/com/badlogic/gdx/tests/g3d/shadows */
		// Create shadow system
		passBatches = new Array<ModelBatch>();
		system = new ClassicalShadowSystem(
				new AABBNearFarAnalyzer(),
				new FixedShadowMapAllocator(2048, 4),
				new BoundingSphereDirectionalAnalyzer(),
				new FrustumLightFilter()
		);
		system.addLight(sun);
		system.addLight(moon);
		system.init();
		for (int i = 0; i < system.getPassQuantity(); i++) {
			passBatches.add(new ModelBatch(system.getPassShaderProvider(i)));
		}
		modelBatch = new ModelBatch(system.getShaderProvider());

		// Input controller for mouse and keyboard events
		inputController = new LightsCameraActionInputController(cam, sun, environment, instanceBox);
		inputController.target = new Vector3(2.8f, 0f, -0.8f);
		Gdx.input.setInputProcessor(inputController);
	}

	@Override
	public void render () {
		inputController.update();

		// Render scene with shadows:
		system.begin(cam, renderables);
		system.update();
		for (int i = 0; i < system.getPassQuantity(); i++) {
			system.begin(i);
			Camera camera;
			while ((camera = system.next()) != null) {
				passBatches.get(i).begin(camera);
				passBatches.get(i).render(renderables, environment);
				passBatches.get(i).end();
			}
			camera = null;
			system.end(i);
		}
		system.end();

		HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

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
