# MiniMine

refeito com LibGDX.

## Requisitos mínimos:

## PC/Notebook:
* Sistema Operacional: Windows/Linux
* Java: 7.
* RAM: 350 MBs.
* OpenGL: 2.

## Celular:
* Sistema Operacional: Android.
* Java: 7.
* RAM: 200 MBs.
* OpenGL: 2.

## Já feito:
1. Gerenciamento de chunks dinâmico.
2. Geração de terreno com Ruido Simplex 2D e 3D pras cavernas, manual.
3. Colisão.
4. Gravidade.
5. Iluminação global.
6. Interface de botões de movimentação.
7. Debug visual.
8. Sistema de construção.
9. Barra Rápida.
10. Mods por Lua no armazenamento externo. (beta)
11. Sistema de ciclo noturno e diário.
12. Nuvens.
13. Salvamento dinamico de mundos binario.
14. Aúdio (ainda temporario até aúdios gravados manualmente).
15. Agachamento (evita cair de bordas dos blocos).
16. Tela de menu e configurações padrão.
17. Divisão de biomas.
18. Água animada via *Gdx.gl.glTexSubImage2D(...)*.
19. Névoa no horizonte.
20. Motor de geração. (beta)
21. Menu de pause.
22. Partículas ao quebrar blocos.
23. 1-2 músicas.
24. Divisão de biomas com rúido celular.

## Modos de jogo:
* 0: espectador. Não sofre gravidade ou colisão com blocos. Seus recursos não acabam
* 1: criativo. Não sofre com gravidade mas colide com blocos. Seus recursos não acabam
* 2: sobrevivencia. Sofre com gravidade e colide com blocos. Seus recursos acabam.

você pode descobrir mais sobre a API Lua em doc.txt.

## Blocos:
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
12. Pedregulho.
13. Cascalho.

## Receitas:
ao clicar no botão de receitas, você pode obter um novo bloco apartir dessa receita.

selecione o bloco em questão como item atual, e clique no botão de receita.

essas são as receitas atuais e seus resultados:

* tronco = tabuas_madeira
* areia = vidro
* folha = tocha

## Geração feita:

Dominio de Deformação.
Erosão Hidraulica.

## Biomas:

1. OCEANO.
2. OCEANO_COSTEIRO.
3. OCEANO_QUENTE.
4. OCEANO_PROFUNDO.
5. PLANICIES.
6. PLANICIES_MONTANHOSAS.
7. FLORESTA.
8. FLORESTA_COSTEIRA.
9. FLORESTA_MONTANHOSA.
10. DESERTO.
11. COLINAS_DESERTO.

## Otimizações:
* Geração em Thread separada com ExecutorService.
* Chaves do tipo *long* para obtenção de chunks.
* Descarte de faces sobrepostas.
* Não renderizar chunks fora do raio de visão.
* Compressão baseada em paleta.
* Otimização com variaveis locais pra compilação em tempo de  execução.
* Cache de chunks modificadas sem Malha.
* O Guloso (Malha Gulosa).
* Pré-computação de erosão.
* Reuso de Arrays utilizados na geração de dados das chunks.
* Iluminação feita por vértices, com niveis por blocos (0-15).
* Sistema hibrido de iluminação e ciclos diários com vértices e shader.
* Compactação de dados de luz em 1 único atributo de vértice.
* Compactação de limites do atlas para *O Guloso* funcionar com 1 atributo de vértice.
* Compactação de posição dos blocos em 1 atributo de vértice.
* buffer de até 256 texturas únicas para dados do atlas (evita mais atributos de vértice).

## Ruídos utilitários:
1. PerlinRuido2D.java
2. PerlinRuido3D.java
3. Simplex2D.java (atual)
4. Simplex3D.java (atual)
5. RidgeRuido2D.java (atual)
6. CelularRuido2D.java (atual)

## compatibilidade:
* Android 4 até Android 15.
* Linux 32-bit e 64-bit.
* Windows XP até Windows 11.

## Desempenho:
FPS de 30 a 60 padrão testado com até 112 chunks ativas (raio de 5).
## uso de mémoria testada:
50 MBs do heap java & 25 MBs do heap nativo. (112 chunks ativas)

## Mods Lua:
você pode criar mods achando a pasta *MiniMine/mods/* no armazenamento externo. Adicione os arquivos Lua necéssarios:

att.lua // será chamado no loop principal

e para adicionar mais de um arquivo individual, adicione o caminho relativo a pasta atual em *MiniMine/mods/arquivos.mini*. os arquivos são separados e carregados por quebra de linha.

## Adicionais:

caso o jogo crashe ou você não tenha visão completa dos logs, visite *MiniMine/debug/logs.txt*, onde logs são acumulados a cada entrada em um mundo.

## Dispositivos usados para testes:
## Celular:
* Modelo: Motorola G41.
* Memória RAM: 4 GB.
* Armazenamento: 128 GB.
* Processador: 8 núcleos, velocidade clock 500 MHz - 2.00 GHz. ARM64.
* OpenGL ES: 3.2.
* JVM: Java VM ART 2.1.0.
* Sistema Operacional: Android 12 64-bit.
* FPS padrão: 40-59.

## PC:
* Placa Mãe: dell optiPlex 780.
* Processador: intel core 2 quad.
* Memória RAM: 4GB(2x2GB DDR3).
* Armazenamento: SSD 256GB.
* Video: intel 4 series(Integrada).
* OpenGL: 2.1.
* Sistema Operaciobal: Linux Mint XCFE 64-bit.
* FPS padrão: 40-59.

## Notebook:
* Modelo: Aspire ES 15.
* Processador: Intel Celeron Quad Core N3450.
* Memória RAM: 4GB DDR3 L.
* Armazenamento: HD 500 GBs.
* Video: Intel HD Graphics.
* OpenGL: 4.5.
* Sistema Operacional: Windows 10.
* FPS padrão: 100-255.

# Comandos de teclado
* **WASD**: controles de movimento.
* **ESPAÇO**: pula/voa.
* **SHIFT**: agacha/desce.
* **DIREITA/ESQUERDA**: com o mouse quebra e coloca blocos.
* **E**: abre o inventario.
* **T**: abre o chat.
* **F1**: abre o modo de debug.
* **R**: fabrica algo com base no item selecionado.
* **ESC**: abre o menu de pausa durante o jogo.

## paleta de cores:
* blocos: unseven.

## Créditos:

**Programação**:
Shiniga-OP
Green
Xaniim

**Efeitos sonoros**:
Shiniga-OP
VDLN7

**Pixel art**:
Shiniga-OP

**Teste da API de mods e documentação ou ambiente de execução**:
VDLN7
Green
Dorganhozo

**Canais:**:
Shiniga-OP: https://youtube.com/@shiniga-op?si=A78wk-sm3EJvgavE
VDLN7: https://youtube.com/@violetbrasilofc?si=Ip8AkZdPnDDdFjGm
Green: https://youtube.com/@greenlevelcreatordev?si=q1HhyS115FbbPhOI
Dorganhozo: https://youtube.com/@dorganzo?si=phKKbJ4P5C87TMJ0
