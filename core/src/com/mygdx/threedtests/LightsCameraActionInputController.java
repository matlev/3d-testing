package com.mygdx.threedtests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class LightsCameraActionInputController extends CameraInputController {

    protected DirectionalShadowLight light;
    protected Environment environment;
    protected ModelInstance actor;

    // Actor movement
    private final Quaternion actorRot = new Quaternion();
    private final Vector3 forwardVector = new Vector3(0, 0, -1f);

    // Mouse
    public int snapToActorButton = Input.Buttons.RIGHT;

    // Keyboard
    protected boolean controlPressed;
    public int controlKey = Input.Keys.CONTROL_LEFT;
    protected boolean changeLightXPressed;
    public int changeLightXKey = Input.Keys.NUMPAD_1;
    protected boolean changeLightYPressed;
    public int changeLightYKey = Input.Keys.NUMPAD_2;
    protected boolean changeLightZPressed;
    public int changeLightZKey = Input.Keys.NUMPAD_3;
    public int toggleDirectionLightKey = Input.Keys.NUMPAD_0;
    protected boolean lightOn = true;

    // Drag support
    private float startX, startY, endX;
    private final Vector3 tmpV1 = new Vector3();
    private int touched;
    private boolean multiTouch;

    public LightsCameraActionInputController(Camera cam, DirectionalShadowLight light, Environment environment, ModelInstance actor) {
        super(cam);

        this.environment = environment;
        this.light = light;
        this.actor = actor;
    }

    @Override
    public boolean keyDown(int keycode) {
        super.keyDown(keycode);

        if (keycode == controlKey) {
            controlPressed = true;
        } else if (keycode == changeLightXKey) {
            changeLightXPressed = true;
        } else if (keycode == changeLightYKey) {
            changeLightYPressed = true;
        } else if (keycode == changeLightZKey) {
            changeLightZPressed = true;
        } else if (keycode == toggleDirectionLightKey) {
            lightOn = !lightOn;
            if (!lightOn) {
                environment.remove(light);
            } else {
                DirectionalLightsAttribute dirLights = ((DirectionalLightsAttribute) environment.get(DirectionalLightsAttribute.Type));
                if (dirLights == null || !dirLights.lights.contains(light, false)) {
                    environment.add(light);
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        super.keyUp(keycode);

        if (keycode == controlKey) {
            controlPressed = false;
        } else if (keycode == changeLightXKey) {
            changeLightXPressed = false;
        } else if (keycode == changeLightYKey) {
            changeLightYPressed = false;
        } else if (keycode == changeLightZKey) {
            changeLightZPressed = false;
        }
        return false;
    }

    @Override
    public void update() {
        final float delta = Gdx.graphics.getDeltaTime();

        // No mouse, camera rotates with actor and follows their movement
        if (touched == 0) {
            if (rotateRightPressed) {
                rotateCamera(-delta, 0);
                rotateActor(-delta);
            }
            if (rotateLeftPressed) {
                rotateCamera(delta, 0);
                rotateActor(delta);
            }
            if (forwardPressed) {
                // Get the forward unit vector in the direction the actor is facing
                actor.transform.getRotation(actorRot);
                tmpV1.set(forwardVector).mul(actorRot);

                // Add the unit vector, scaled to the magnitude of movement in delta time, to the actor matrix
                actor.transform.trn(tmpV1.scl(delta * translateUnits));

                // Update the camera position and its target by the actor's movement vector
                camera.position.add(tmpV1);
                target.add(tmpV1);
            }
            if (backwardPressed) {
//                tmpV1.set(camera.direction.x, 0, camera.direction.z).scl(-delta * translateUnits);
//                camera.translate(tmpV1);
//                actor.transform.translate(tmpV1);
//                target.add(tmpV1);
            }
        }



        camera.update();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touched += 1;
        multiTouch = touched > 1;
        if (multiTouch) {
            this.button = snapToActorButton;
            button = this.button;
        }
        startX = screenX;
        startY = screenY;

        // Snap actor to current camera vector
        if (button == snapToActorButton) {
            rotateActor(endX);
            endX = 0f;
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        touched -= 1;
        multiTouch = touched > 1;
        if (touched > 0) {
            this.button = button == snapToActorButton ? rotateButton : snapToActorButton;
        }
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        final float deltaX = (screenX - startX) / Gdx.graphics.getWidth();
        final float deltaY = (startY - screenY) / Gdx.graphics.getHeight();
        startX = screenX;
        startY = screenY;
        return process(deltaX, deltaY, button);
    }

    protected boolean process (float deltaX, float deltaY, int button) {
        // Rotate only the camera
        if (button == rotateButton) {
            endX += deltaX;
            rotateCamera(deltaX, deltaY);
        }
        // Rotate actor with camera
        else if (button == snapToActorButton) {
            rotateCamera(deltaX, deltaY);
            rotateActor(deltaX);
        }
        // Zoom in camera
        else if (button == forwardButton) {
            camera.translate(tmpV1.set(camera.direction).scl(deltaY * translateUnits));
            if (forwardTarget) target.add(tmpV1);
        }
        if (autoUpdate) camera.update();
        return true;
    }

    private void rotateCamera(float deltaX, float deltaY) {
        tmpV1.set(camera.direction).crs(camera.up).y = 0f;
        camera.rotateAround(target, tmpV1.nor(), deltaY * rotateAngle);
        camera.rotateAround(target, Vector3.Y, deltaX * -rotateAngle);
    }

    private void rotateActor(float deltaX) {
        actor.transform.rotate(Vector3.Y, deltaX * -rotateAngle);
    }

}
