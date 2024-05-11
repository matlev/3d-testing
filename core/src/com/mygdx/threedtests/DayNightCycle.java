package com.mygdx.threedtests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.math.Vector3;

public class DayNightCycle {

    protected  Environment environment;
    protected  DirectionalShadowLight sun, moon;
    protected DirectionalShadowLight currentLight;
    protected  float dayLength;
    private float time = 0;
    protected final Vector3 startPosition = new Vector3(17f, -1f, 3f).nor();
    protected final Vector3 endPosition = new Vector3(-17f, -1f, -3f).nor();
    protected final Vector3 halfWayPosition = Vector3.Y.scl(-1f);
    private Vector3 to, from;
    float halfPhaseRatio;

    public DayNightCycle(
            Environment environment,
            DirectionalShadowLight sun,
            DirectionalShadowLight moon,
            float lengthOfDayInSeconds) {
        this.environment = environment;
        this.sun = sun;
        this.moon = moon;
        dayLength = lengthOfDayInSeconds;
    }

    public void update() {
        float delta = Gdx.graphics.getDeltaTime();
        time += delta;

        // A new day, make sure we reset the sun, and moon if we have one.
        if (time > dayLength) {
            time -= dayLength;
            sun.direction.set(startPosition);
            sun.color.set(1, 1, 1, 1);
            if (moon != null) {
                moon.direction.set(startPosition);
                moon.color.set(0, 0, 0, 1);
            }
        }

        float ratio = time / dayLength;
        boolean dayTime = ratio <= 0.5f;

        if (dayTime) {
            ratio *= 2;
            currentLight = sun;
        } else {
            ratio = (ratio - 0.5f) * 2;
            currentLight = moon;
            sun.color.set(0, 0, 0, 1);
            if (moon != null) {
                moon.color.set(0.2f, 0.2f, 0.2f, 1);
            }
        }

        if (ratio <= 0.5f) {
            from = startPosition;
            to = halfWayPosition;
            halfPhaseRatio = ratio * 2;
        } else {
            from = halfWayPosition;
            to = endPosition;
            halfPhaseRatio = (ratio - 0.5f) * 2;
        }
        if (currentLight != null) {
            currentLight.direction.set(from.cpy().lerp(to, halfPhaseRatio));
        }
    }

}
