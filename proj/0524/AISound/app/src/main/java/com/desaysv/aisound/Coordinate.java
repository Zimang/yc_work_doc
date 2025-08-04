package com.desaysv.aisound;

import com.desaysv.sceneengine.util.PxUtil;

public class Coordinate {
    public int x;
    public int y;

    public int z;

    Coordinate(int _x, int _y){
        this.x = _x;
        this.y = _y;
    }

    public static Coordinate getZcCenter(){
        return new Coordinate((int) PxUtil.dpTOpx(490),(int)PxUtil.dpTOpx(262));
    }
}
