package games.rednblack.editor.renderer.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.components.additional.ButtonComponent;
import games.rednblack.editor.renderer.utils.TransformMathUtils;

@All(ButtonComponent.class)
public class ButtonSystem extends BaseEntitySystem {

    protected ComponentMapper<ButtonComponent> buttonComponentMapper;
    protected ComponentMapper<DimensionsComponent> dimensionsComponentMapper;
    protected ComponentMapper<NodeComponent> nodeComponentMapper;
    protected ComponentMapper<MainItemComponent> mainItemComponentMapper;
    protected ComponentMapper<ViewPortComponent> viewPortComponentMapper;
    protected ComponentMapper<ZIndexComponent> zIndexComponentMapper;
    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<ParentNodeComponent> parentMapper;

    private final Vector2 tmp = new Vector2();

    private int inputHoldEntity = -1;

    public ButtonSystem(Aspect.Builder aspect) {
        super(aspect);
    }

    public ButtonSystem() {
    }

    @Override
    protected final void processSystem() {
        IntBag actives = subscription.getEntities();
        int[] ids = actives.getData();
        for (int i = actives.size() - 1; i >= 0; i--) {
            process(ids[i]);
        }
    }

    protected void process(int entity) {
        NodeComponent nodeComponent = nodeComponentMapper.get(entity);
        if (nodeComponent == null) return;

        ViewPortComponent camera = viewPortComponentMapper.get(entity);
        if (camera != null) {
            //Override visibility when editing the button
            for (int i = 0; i < nodeComponent.children.size; i++) {
                Integer childEntity = nodeComponent.children.get(i);
                MainItemComponent childMainItemComponent = mainItemComponentMapper.get(childEntity);
                childMainItemComponent.visible = true;
            }
            return;
        }

        //Check if another input has acquired click focus
        if ((inputHoldEntity != entity && inputHoldEntity != -1)) return;

        boolean isTouched = isTouched(entity);
        boolean isChecked = isChecked(entity);
        for (int i = 0; i < nodeComponent.children.size; i++) {
            Integer childEntity = nodeComponent.children.get(i);
            MainItemComponent childMainItemComponent = mainItemComponentMapper.get(childEntity);
            ZIndexComponent childZComponent = zIndexComponentMapper.get(childEntity);
            if (isTouched) {
                inputHoldEntity = entity;
            } else {
                inputHoldEntity = -1;
            }

            if (isTouched || isChecked) {
                if (childZComponent.layerName.equals("normal")) {
                    childMainItemComponent.visible = false;
                }
                if (childZComponent.layerName.equals("pressed")) {
                    childMainItemComponent.visible = true;
                }
            } else {
                if (childZComponent.layerName.equals("normal")) {
                    childMainItemComponent.visible = true;
                }
                if (childZComponent.layerName.equals("pressed")) {
                    childMainItemComponent.visible = false;
                }
            }
        }
    }

    private boolean isChecked(int entity) {
        ButtonComponent buttonComponent = buttonComponentMapper.get(entity);
        return buttonComponent.isChecked;
    }

    private boolean isTouched(int entity) {
        ButtonComponent buttonComponent = buttonComponentMapper.get(entity);
        if (Gdx.input.isTouched()) {
            DimensionsComponent dimensionsComponent = dimensionsComponentMapper.get(entity);
            tmp.set(Gdx.input.getX(), Gdx.input.getY());

            TransformMathUtils.globalToLocalCoordinates(entity, tmp, transformMapper, parentMapper, viewPortComponentMapper);

            if (dimensionsComponent.hit(tmp.x, tmp.y)) {
                buttonComponent.setTouchState(true);
                return true;
            }
        }
        buttonComponent.setTouchState(false);
        return false;
    }
}
