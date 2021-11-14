package games.rednblack.editor.renderer.factory.v2;

import com.artemis.ComponentMapper;
import com.artemis.EntityTransmuter;
import com.artemis.EntityTransmuterFactory;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;
import games.rednblack.editor.renderer.box2dLight.RayHandler;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.normal.NormalMapRendering;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.utils.ABAtlasRegion;

public class SimpleImageComponentFactoryV2 extends ComponentFactoryV2 {

    protected ComponentMapper<TextureRegionComponent> textureRegionCM;
    protected ComponentMapper<NormalMapRendering> normalMapRenderingCM;

    private final EntityTransmuter transmuter;

    public SimpleImageComponentFactoryV2(com.artemis.World engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        super(engine, rayHandler, world, rm);
        transmuter = new EntityTransmuterFactory(engine)
                .add(TextureRegionComponent.class)
                .add(NormalMapRendering.class)
                .build();
    }

    @Override
    public void transmuteEntity(int entity) {
        transmuter.transmute(entity);
    }

    @Override
    public int getEntityType() {
        return EntityFactoryV2.IMAGE_TYPE;
    }

    @Override
    public void setInitialData(int entity, Object data) {
        textureRegionCM.get(entity).regionName = (String) data;
    }

    @Override
    protected void initializeTransientComponents(int entity) {
        super.initializeTransientComponents(entity);

        TextureRegionComponent component = textureRegionCM.get(entity);
        engine.inject(component);

        if (rm.hasTextureRegion(component.regionName + ".normal")) {
            TextureAtlas.AtlasRegion regionDiffuse = (TextureAtlas.AtlasRegion) rm.getTextureRegion(component.regionName);
            TextureAtlas.AtlasRegion normalRegion = (TextureAtlas.AtlasRegion) rm.getTextureRegion(component.regionName + ".normal");
            component.region = new ABAtlasRegion(regionDiffuse, normalRegion, normalMapRenderingCM.get(entity));
        } else {
            normalMapRenderingCM.remove(entity);
            component.region = rm.getTextureRegion(component.regionName);
        }
    }

    @Override
    protected void initializeDimensionsComponent(int entity) {
        TextureRegionComponent component = textureRegionCM.get(entity);
        DimensionsComponent dimension = dimensionsCM.get(entity);

        ResolutionEntryVO resolutionEntryVO = rm.getLoadedResolution();
        ProjectInfoVO projectInfoVO = rm.getProjectVO();
        float multiplier = resolutionEntryVO.getMultiplier(rm.getProjectVO().originalResolution);

        dimension.width = (float) component.region.getRegionWidth() * multiplier / projectInfoVO.pixelToWorld;
        dimension.height = (float) component.region.getRegionHeight() * multiplier / projectInfoVO.pixelToWorld;

        updatePolygons(entity);
    }

    private void updatePolygons(int entity) {
        DimensionsComponent dimensionsComponent = dimensionsCM.get(entity);
        PolygonComponent polygonComponent = polygonCM.get(entity);

        TextureRegionComponent textureRegionComponent = textureRegionCM.get(entity);
        if (textureRegionComponent.isPolygon && polygonComponent != null && polygonComponent.vertices != null) {
            textureRegionComponent.setPolygonSprite(polygonComponent);
            dimensionsComponent.setPolygon(polygonComponent);
        }
    }
}
