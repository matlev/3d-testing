package com.mygdx.threedtests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class LightsCameraActionInputController extends CameraInputController {

    protected DirectionalShadowLight light;
    private final Color lightColor;
    protected Environment environment;
    protected ModelInstance actor;

    // Actor movement
    private final Quaternion actorRot = new Quaternion();
    private final Vector3 forwardVector = new Vector3(0, 0, -1f);
    private final Vector3 orthoVector = new Vector3(1f, 0, 0);
    private final Vector3 upVector = new Vector3(0, 1f, 0);
    private Vector3 sideVector = new Vector3();
    private final Matrix4 rotMatrix = new Matrix4();

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
    private float startX, startY;
    private final Vector3 tmpV1 = new Vector3();
    private int touched;
    private boolean multiTouch;

    public LightsCameraActionInputController(Camera cam, DirectionalShadowLight light, Environment environment, ModelInstance actor) {
        super(cam);

        this.environment = environment;
        this.light = light;
        this.lightColor = light.color.cpy();
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
                light.setColor(0, 0, 0, 1);
            } else {
                light.setColor(lightColor);
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

        // Repetitive on purpose so that each key-press maintains its own logic handling.
        if (rotateRightPressed) {
            if (touched == 0) {
                rotateCamera(-delta, 0);
                rotateActor(-delta);
            }
            else if (this.button == snapToActorButton) {
                moveActor(-delta, orthoVector);
                moveCamera(tmpV1);
            }
            else if (this.button == rotateButton) {
                rotateActor(-delta);
            }
        }
        if (rotateLeftPressed) {
            if (touched == 0) {
                rotateCamera(delta, 0);
                rotateActor(delta);
            } else if (this.button == snapToActorButton) {
                moveActor(delta, orthoVector);
                moveCamera(tmpV1);
            }
            else if (this.button == rotateButton) {
                rotateActor(delta);
            }
        }
        if (forwardPressed || touched == 2) {
            moveActor(delta, forwardVector);
            moveCamera(tmpV1);
        }
        if (backwardPressed) {
            moveActor(-delta, forwardVector);
            moveCamera(tmpV1);
        }

        camera.update();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Gdx.input.setCursorCatched(true);

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
            snapActorToCameraOrientation();
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        touched -= 1;
        multiTouch = touched > 1;
        if (touched > 0) {
            // Button is the just-release button. this.button is the button still being pressed.
            this.button = button == snapToActorButton ? rotateButton : snapToActorButton;
        } else {
            Gdx.input.setCursorCatched(false);
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
        camera.update();
        return true;
    }

    private void rotateCamera(float deltaX, float deltaY) {
        tmpV1.set(camera.direction).crs(camera.up).y = 0f;
        camera.rotateAround(target, tmpV1.nor(), deltaY * rotateAngle);
        camera.rotateAround(target, Vector3.Y, deltaX * rotateAngle);
    }

    /**
     * Move the camera and its target in a vector direction.
     *
     * @param translationVector the direction and magnitude to move.
     */
    private void moveCamera(Vector3 translationVector) {
        camera.position.add(translationVector);
        target.add(translationVector);
    }

    private void rotateActor(float deltaX) {
        actor.transform.rotate(Vector3.Y, deltaX * rotateAngle);
    }

    private void snapActorToCameraOrientation() {
        tmpV1.set(camera.direction.x, 0, camera.direction.z).nor(); // forward
        sideVector = upVector.cpy().crs(tmpV1).nor(); // right
        rotMatrix.idt();
        rotMatrix.val[Matrix4.M00] = sideVector.x;
        rotMatrix.val[Matrix4.M10] = sideVector.y;
        rotMatrix.val[Matrix4.M20] = sideVector.z;
        rotMatrix.val[Matrix4.M01] = upVector.x;
        rotMatrix.val[Matrix4.M11] = upVector.y;
        rotMatrix.val[Matrix4.M21] = upVector.z;
        rotMatrix.val[Matrix4.M02] = tmpV1.x;
        rotMatrix.val[Matrix4.M12] = tmpV1.y;
        rotMatrix.val[Matrix4.M22] = tmpV1.z;

        actor.transform.getTranslation(tmpV1);
        actor.transform.set(rotMatrix).setTranslation(tmpV1).rotate(upVector, 180f);
    }

    private void moveActor(float delta, Vector3 movementAxis) {
        // Get the forward unit vector in the direction the actor is facing
        actor.transform.getRotation(actorRot);
        tmpV1.set(movementAxis).mul(actorRot);

        // Add the unit vector, scaled to the magnitude of movement in delta time, to the actor matrix
        actor.transform.trn(tmpV1.scl(delta * translateUnits));
    }

}
