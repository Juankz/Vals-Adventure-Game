package com.epifania.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

/**
 * Created by juan on 6/23/16.
 */
public class PasswordComponent implements Component {
    public final Array<PasswordPieceComponent> keys = new Array<PasswordPieceComponent>();
    public boolean unlocked = false;
}
