package me.luxtix.haybale.features.modules.movement;

import me.luxtix.haybale.features.modules.Module;
import me.luxtix.haybale.features.setting.Setting;

public
class StepRW extends Module {
    public Setting < Integer > height = register ( new Setting ( "Height" , 2 , 0 , 5 ) );

    public
    StepRW ( ) {
        super ( "StepRW" , "Allows you to step up blocks" , Module.Category.MOVEMENT , true , false , false );

    }

    @Override
    public
    void onUpdate ( ) {
        if ( fullNullCheck ( ) ) return;
        mc.player.stepHeight = 2.0f;
    }

    @Override
    public
    void onDisable ( ) {
        super.onDisable ( );
        mc.player.stepHeight = 0.6f;
    }
}