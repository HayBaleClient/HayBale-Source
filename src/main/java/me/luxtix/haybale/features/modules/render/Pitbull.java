package me.travis.wurstplusthree.hack.hacks.render;

import me.luxtix.haybale.features.modules.render;
import me.luxtix.haybale.setting.ColourSetting;
import me.luxtix.haybale.util.Colour;

/**
 * @author Madmegsox1
 * @since 28/04/2021
 */

@Hack.Registration(name = "RagettiiESP", description = "makes everyones skin pitbull", category = Hack.Category.RENDER, isListening = false)
public class Pitbull extends Hack {

    public static Pitbull INSTANCE;

    public Pitbull(){
        INSTANCE = this;
    }

    public ColourSetting texture = new ColourSetting("Texture",new Colour(255,255,255, 255), this);

}
