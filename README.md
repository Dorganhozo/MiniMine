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

## blocos:
Grama, Terra, Pedra.

## otimizações:
Reuso de objetos Mesh.
Face culling.
Frustrum culling (incluindo por distância e direção de olhar).
Compactação de dados (byte[] blocos = new byte[16 * 255 * 16 / 4]).

## compatibilidade:
Android 4 até Android 14.

## uso de memória
81 chunks ativas consomem de 10 a 28 MBs de espaço do heap.

FPS de 20 padrão testado.

## dispositivo usado para testes:
Motorola G41, 4 GB de RAM, 128 GB de armazenamento. 8 núcleos, velocidade clock 500 MHz - 2.00 GHz. ARM64. Android 12, OpenGL ES 3.2. Java VM ART 2.1.0.
