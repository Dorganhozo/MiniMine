# MiniMine

refeito com LibGDX.

## já feito:
1. Gerenciamento de chunks dinâmico.
2. Geração de terreno com Simplex noise 2D manual.
3. Colisão.
4. Gravidade.
5. Iluminação por vértices.
6. Interface de botões de movimentação.
7. Debug visual.
8. Sistema de construção.
9. Hotbar.
10. Mods por Lua/JavaScript no armazenamento externo. (beta)
11. Sistema de ciclo noturno e diário.
12. Nuvens.
13. salvamento dinamico de mundos binario.

## modos de jogo:
0: espectador. Não sofre gravidade ou colisão com blocos. Seus recursos não acabam
1: criativo. Não sofre com gravidade mas colide com blocos. Seus recursos não acabam
2: sobrevivencia. Sofre com gravidade e colide com blocos. Seus recursos acabam.

você pode descobrir mais sobre a API Lua em doc.txt.

## blocos:
1. Ar.
2. Grama.
3. Terra.
4. Pedra.
5. Água.
6. Areia.
7. Tronco de madeira.
8. Bloco de folhas.
9. Tabua de madeira.
10. Cacto.
11. Vidro.

## otimizações:
Geração em Thread separada com ExecutorService.
Reuso de objetos Mesh.
Reuso de objetos ChunkUtil.Chave.
Reuso de 1 objeto Matrix4 para todas as chunks.
Face culling global.
Frustrum culling (incluindo por distância e direção de olhar).
Compressão baseada em paleta.

## ruídos utilitários:
1. Perlin Noise 2D.
2. Perlin Noise 3D.
3. Simplex Noise 2D.
4. Simplex Noise 3D.

## compatibilidade:
Android 4 até Android 14.

## desempenho:
FPS de 30 a 40 padrão testado com até 121 chunks (raio de 5 por padrão).

## mods Lua:
você pode criar mods achando a pasta *MiniMine/mods/* no armazenamento externo. Adicione os arquivos Lua necéssarios:

att.lua // será chamado no loop principal

e para adicionar mais de um arquivo individual, adicione o caminho relativo a pasta atual em *MiniMine/mods/arquivos.mini*. os arquivos são separados e carregados por quebra de linha.

## mods JavaScript:

adicione seus scripts em "MiniMine/mods/arquivos.html".

sem documentação ainda.

## adicionais:

caso o jogo crashe ou você não tenha visão completa dos logs, visite *MiniMine/debug/logs.txt*, onde logs são acumulados a cada entrada em um mundo.

## dispositivo usado para testes:
Motorola G41, 4 GB de RAM, 128 GB de armazenamento. 8 núcleos, velocidade clock 500 MHz - 2.00 GHz. ARM64. Android 12, OpenGL ES 3.2. Java VM ART 2.1.0.
