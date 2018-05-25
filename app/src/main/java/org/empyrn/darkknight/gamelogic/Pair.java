package org.empyrn.darkknight.gamelogic;

import android.util.Log;

/**
 * A small helper class that makes it possible to return two values from a function.
 * @author petero
 */

public final class Pair<T1, T2> {
    public final T1 first;
    public final T2 second;

    public Pair(T1 first, T2 second) {
        Log.d("lol","inside pair method",new Exception());
        this.first = first;
        this.second = second;
    }
}