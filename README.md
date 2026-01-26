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
10. Mods por Lua no armazenamento externo. (beta)
11. Sistema de ciclo noturno e diário.
12. Nuvens.
13. Salvamento dinamico de mundos binario.
14. Aúdio (ainda temporario até aúdios gravados manualmente).
15. Agachamento (evita cair de bordas dos blocos).
16. Tela de menu e configurações padrão.
17. Divisão de biomas.

## modos de jogo:
0: espectador. Não sofre gravidade ou colisão com blocos. Seus recursos não acabam
1: criativo. Não sofre com gravidade mas colide com blocos. Seus recursos não acabam
2: sobrevivencia. Sofre com gravidade e colide com blocos. Seus recursos acabam.

você pode descobrir mais sobre a API Lua em doc.txt.

## blocos:
0. Ar.
1. Grama.
2. Terra.
3. Pedra.
4. Água.
5. Areia.
6. Tronco de madeira.
7. Bloco de folhas.
8. Tabua de madeira.
9. Cacto.
10. Vidro.
11. Tocha.

## receitas:
ao clicar no botão de receitas, você pode obter um novo bloco apartir dessa receita.

selecione o bloco em questão como item atual, e clique no botão de receita.

essas são as receitas atuais e seus resultados:

tronco = tabuas_madeira
areia = vidro
folha = tocha

## otimizações:
Geração em Thread separada com ExecutorService.
Reuso de objetos Chave parq chunks.
Reuso de 1 objeto Matrix4 para todas as chunks.
Face culling global.
Frustrum culling (incluindo por distância e direção de olhar).
Compressão baseada em paleta.
Otimização na atualização com variaveis locais (JIT).
Proteção contra vazamentos de memória nativa pré calculando a quantidade de chunks.
Cache de luz.
Cache de chunks modificadas sem Mesh.
Implementação do rúido Simplex 2D, 3D em C via JNI (beta, somente em ARM64).
Cache no rúido Simplex 2D.

## ruídos utilitários:
1. Perlin Noise 2D.
2. Perlin Noise 3D.
3. Simplex Noise 2D.
4. Simplex Noise 3D.

## compatibilidade:
Android 4 até Android 14.
Linux Mint.

## desempenho:
FPS de 30 a 59 padrão testado com até 280 chunks ativas (raio de 8).

## mods Lua:
você pode criar mods achando a pasta *MiniMine/mods/* no armazenamento externo. Adicione os arquivos Lua necéssarios:

att.lua // será chamado no loop principal

e para adicionar mais de um arquivo individual, adicione o caminho relativo a pasta atual em *MiniMine/mods/arquivos.mini*. os arquivos são separados e carregados por quebra de linha.

## mods JavaScript (descontinuado por consuno excessivo de Threads):

adicione seus scripts em "MiniMine/mods/arquivos.html".

sem documentação ainda.

## adicionais:

caso o jogo crashe ou você não tenha visão completa dos logs, visite *MiniMine/debug/logs.txt*, onde logs são acumulados a cada entrada em um mundo.

## dispositivos usados para testes:
Celular: Motorola G41, 4 GB de RAM, 128 GB de armazenamento. 8 núcleos, velocidade clock 500 MHz - 2.00 GHz. ARM64. Android 12, OpenGL ES 3.2. Java VM ART 2.1.0.
Computador: dell optiPlex 780, intel core 2 quad memória: 4GB RAM(2x2GB DDR3) armazenamento: SSD 256GB + HD 500GB, video intel 4 series(Integrada), Linux Mint

## Créditos:

**Programação**:
Shiniga-OP

**Efeitos sonoros**:
Shiniga-OP
VDLN7

**Pixel art**:
Shiniga-OP

**Teste da API de mods e documentação**:
VDLN7
Green

**Canais:**:
Shiniga-OP: https://youtube.com/@shiniga-op?si=A78wk-sm3EJvgavE
VDLN7: https://youtube.com/@violetbrasilofc?si=Ip8AkZdPnDDdFjGm
Green: https://youtube.com/@greenlevelcreatordev?si=q1HhyS115FbbPhOI
