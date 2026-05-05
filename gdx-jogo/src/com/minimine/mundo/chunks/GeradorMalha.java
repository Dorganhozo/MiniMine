package com.minimine.mundo.chunks;

import com.minimine.utils.arrays.FloatArrayUtil;
import com.minimine.utils.arrays.ShortArrayUtil;

public interface GeradorMalha {
	void attMalha(Chunk chunk, FloatArrayUtil verts, ShortArrayUtil idcSolidos, ShortArrayUtil idcTransp);
}
