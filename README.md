# MiniMine

refeito com LibGDX.

## já feito:
Gerenciamento de chunks dinâmico.
Geração de terreno com Perlin noise 2D e 3D manuais.
Colisão.
Gravidade.
Iluminação por vértices.
Interface de botões de movimentação.
Debug visual.
Sistema de construção (beta).
Hotbar.

## modos de jogo:
0: espectador. Não sofre gravidade ou colisão com blocos. Seus recursos não acabam
1: criativo. Não sofre com gravidade mas colide com blocos. Seus recursos não acabam
2: sobrevivencia. Sofre com gravidade e colide com blocos. Seus recursos acabam.

## blocos:
1. Ar.
2. Grama.
3. Terra.
4. Pedra.
5. Água.
6. Areia.
7. Tronco de madeira.
8. Bloco de folhas.

## otimizações:
Geração em Thread separada com ExecutorService.
Reuso de objetos Mesh.
Reuso de objetos ChunkUtil.Chave.
Reuso de 1 objeto Matrix4 para todas as chunks.
Face culling global.
Frustrum culling (incluindo por distância e direção de olhar).
Compactação de dados (byte[] blocos = new byte[16 * 255 * 16 / 2]), suporta até 8 blocos por byte.

## compatibilidade:
Android 4 até Android 14.

## desempenho:
FPS de 36 a 56 padrão testado com até 121 chunks.

## dispositivo usado para testes:
Motorola G41, 4 GB de RAM, 128 GB de armazenamento. 8 núcleos, velocidade clock 500 MHz - 2.00 GHz. ARM64. Android 12, OpenGL ES 3.2. Java VM ART 2.1.0.
