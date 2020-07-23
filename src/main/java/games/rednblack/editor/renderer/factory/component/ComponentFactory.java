/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.renderer.factory.component;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.physics.box2d.World;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by azakhary on 5/22/2015.
 */
public abstract class ComponentFactory {

    protected IResourceRetriever rm;
    protected RayHandler rayHandler;
    protected World world;
    protected PooledEngine engine;

    protected ComponentMapper<NodeComponent> nodeComponentMapper;

    public ComponentFactory() {
        nodeComponentMapper = ComponentMapper.getFor(NodeComponent.class);
    }

    public ComponentFactory(PooledEngine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        this();
        injectDependencies(engine, rayHandler, world, rm);
    }

    public void injectDependencies(PooledEngine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        this.engine = engine;
        this.rayHandler = rayHandler;
        this.world = world;
        this.rm = rm;
    }

    public abstract void createComponents(Entity root, Entity entity, MainItemVO vo);

    protected void createCommonComponents(Entity entity, MainItemVO vo, int entityType) {
        DimensionsComponent dimensionsComponent = createDimensionsComponent(entity, vo);
        createBoundingBoxComponent(entity, vo);
        createMainItemComponent(entity, vo, entityType);
        createTransformComponent(entity, vo, dimensionsComponent);
        createTintComponent(entity, vo);
        createZIndexComponent(entity, vo);
        createScriptComponent(entity, vo);
        createMeshComponent(entity, vo);
        createPhysicsComponents(entity, vo);
        createLightComponents(entity, vo);
        createShaderComponent(entity, vo);
    }

    protected BoundingBoxComponent createBoundingBoxComponent(Entity entity, MainItemVO vo) {
        BoundingBoxComponent component = engine.createComponent(BoundingBoxComponent.class);
        entity.add(component);
        return component;
    }

    protected ShaderComponent createShaderComponent(Entity entity, MainItemVO vo) {
    	if(vo.shaderName == null || vo.shaderName.isEmpty()){
    		return null;
    	}
		ShaderComponent component = engine.createComponent(ShaderComponent.class);
		component.setShader(vo.shaderName, rm.getShaderProgram(vo.shaderName));
		entity.add(component);
		return component;
	}

	protected MainItemComponent createMainItemComponent(Entity entity, MainItemVO vo, int entityType) {
        MainItemComponent component = engine.createComponent(MainItemComponent.class);
        component.setCustomVarString(vo.customVars);
        component.uniqueId = vo.uniqueId;
        component.itemIdentifier = vo.itemIdentifier;
        component.libraryLink = vo.itemName;
        if(vo.tags != null) {
            component.tags = new HashSet<>(Arrays.asList(vo.tags));
        }
        component.entityType = entityType;

        entity.add(component);

        return component;
    }

    protected TransformComponent createTransformComponent(Entity entity, MainItemVO vo, DimensionsComponent dimensionsComponent) {
        TransformComponent component = engine.createComponent(TransformComponent.class);
        component.rotation = vo.rotation;
        component.scaleX = vo.scaleX;
        component.scaleY = vo.scaleY;
        component.x = vo.x;
        component.y = vo.y;

        if(Float.isNaN(vo.originX)) component.originX = dimensionsComponent.width/2f;
        else component.originX = vo.originX;

        if(Float.isNaN(vo.originY)) component.originY = dimensionsComponent.height/2f;
        else component.originY = vo.originY;

        entity.add(component);

        return component;
    }

    protected abstract DimensionsComponent createDimensionsComponent(Entity entity, MainItemVO vo);

    protected TintComponent createTintComponent(Entity entity, MainItemVO vo) {
        TintComponent component = engine.createComponent(TintComponent.class);
        component.color.set(vo.tint[0], vo.tint[1], vo.tint[2], vo.tint[3]);

        entity.add(component);

        return component;
    }

    protected ZIndexComponent createZIndexComponent(Entity entity, MainItemVO vo) {
        ZIndexComponent component = engine.createComponent(ZIndexComponent.class);

        if(vo.layerName == "" || vo.layerName == null) vo.layerName = "Default";

        component.layerName = vo.layerName;
        component.setZIndex(vo.zIndex);
        component.needReOrder = false;
        entity.add(component);

        return component;
    }

    protected ScriptComponent createScriptComponent(Entity entity, MainItemVO vo) {
        ScriptComponent component = engine.createComponent(ScriptComponent.class);
        entity.add(component);
        return component;
    }

    protected ParentNodeComponent createParentNodeComponent(Entity root, Entity entity) {
        ParentNodeComponent component = engine.createComponent(ParentNodeComponent.class);
        component.parentEntity = root;
        entity.add(component);

        //set visible to true depending on parent
        // TODO: I do not likes this part
        MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
        LayerMapComponent layerMapComponent = ComponentRetriever.get(root, LayerMapComponent.class);
        ZIndexComponent zIndexComponent = ComponentRetriever.get(root, ZIndexComponent.class);
        mainItemComponent.visible = layerMapComponent.isVisible(zIndexComponent.layerName);

        return component;
    }

    protected void createNodeComponent(Entity root, Entity entity) {
        NodeComponent component = nodeComponentMapper.get(root);
        component.children.add(entity);
    }

    protected void createPhysicsComponents(Entity entity, MainItemVO vo) {
        if(vo.physics == null){
            return;
        }

        createPhysicsBodyPropertiesComponent(entity, vo);
    }

    protected PhysicsBodyComponent createPhysicsBodyPropertiesComponent(Entity entity, MainItemVO vo) {
        PhysicsBodyComponent component = engine.createComponent(PhysicsBodyComponent.class);
        component.allowSleep = vo.physics.allowSleep;
        component.sensor = vo.physics.sensor;
        component.awake = vo.physics.awake;
        component.bodyType = vo.physics.bodyType;
        component.bullet = vo.physics.bullet;
        component.centerOfMass = vo.physics.centerOfMass;
        component.damping = vo.physics.damping;
        component.density = vo.physics.density;
        component.friction = vo.physics.friction;
        component.gravityScale = vo.physics.gravityScale;
        component.mass = vo.physics.mass;
        component.restitution = vo.physics.restitution;
        component.rotationalInertia = vo.physics.rotationalInertia;
        component.angularDamping = vo.physics.angularDamping;
        component.fixedRotation = vo.physics.fixedRotation;

        entity.add(component);

        return component;
    }

    protected LightBodyComponent createLightComponents(Entity entity, MainItemVO vo) {
        if(vo.light == null){
            return null;
        }

        LightBodyComponent component = engine.createComponent(LightBodyComponent.class);
        component.rays = vo.light.rays;
        component.color = vo.light.color;
        component.distance = vo.light.distance;
        component.rayDirection = vo.light.rayDirection;
        component.softnessLength = vo.light.softnessLength;
        component.isXRay = vo.light.isXRay;
        component.isStatic = vo.light.isStatic;
        component.isSoft = vo.light.isSoft;
        component.isActive = vo.light.isActive;

        entity.add(component);
        return component;
    }

    protected PolygonComponent createMeshComponent(Entity entity, MainItemVO vo) {
        PolygonComponent component = engine.createComponent(PolygonComponent.class);
        if(vo.shape != null) {
            component.vertices = vo.shape.polygons.clone();
            entity.add(component);

            return component;
        }
        return null;
    }

    public void setResourceManager(IResourceRetriever rm) {
        this.rm = rm;
    }

}
