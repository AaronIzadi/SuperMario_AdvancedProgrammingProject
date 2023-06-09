package SuperMario.graphic.view.states;

import java.awt.*;
import java.util.ArrayList;

public enum MapSelection {

    WORLD_1(0),
    WORLD_2(1),
    WORLD_3(2),
    CROSSOVER_1(3),
    CROSSOVER_2(4),
    CROSSOVER_3(5),
    BOSS_FIGHT(6);

    private final int worldNumber;
    private final ArrayList<String> maps = new ArrayList<>();
    private final MapSelectionItem[] mapSelectionItems;

    MapSelection(int lineNumber) {
        getMaps();
        this.worldNumber = lineNumber;
        this.mapSelectionItems = createItems(this.maps);
    }

    public MapSelection getSelection(int number) {
        if (number == 0)
            return WORLD_1;
        else if (number == 1)
            return WORLD_2;
        else if (number == 2)
            return WORLD_3;
        else if (number == 3) {
            return CROSSOVER_1;
        } else if (number == 4) {
            return CROSSOVER_2;
        } else if (number == 5) {
            return CROSSOVER_3;
        } else if (number == 6)
            return BOSS_FIGHT;
        else
            return null;
    }

    public int getWorldNumber() {
        return worldNumber;
    }

    private void getMaps() {
        maps.add("Map 1.png");
        maps.add("Map 2.png");
        maps.add("Map 3.png");
        maps.add("crossover 1.png");
        maps.add("crossover 2.png");
        maps.add("crossover 3.png");
        maps.add("Map 4.png");
    }

    public String getMapPath(int worldNumber) {
        return maps.get(worldNumber);
    }

    private MapSelectionItem[] createItems(ArrayList<String> maps) {
        if (maps == null)
            return null;

        int defaultGridSize = 100;
        MapSelectionItem[] items = new MapSelectionItem[maps.size()];
        for (int i = 0; i < items.length; i++) {
            Point location = new Point(0, (i + 1) * defaultGridSize + 200);
            items[i] = new MapSelectionItem(maps.get(i), location);
        }

        return items;
    }

    public String selectMap(int index) {
        if (index < mapSelectionItems.length && index > -1) {
            return mapSelectionItems[index].getName();
        }
        return null;
    }
}
