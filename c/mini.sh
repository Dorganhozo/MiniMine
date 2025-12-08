ARQ=$1
cp $CASA/AideProjects/MiniMine/c/$ARQ.c $HOME

clang -shared -fPIC -O3 -ffast-math \
  -I/data/data/com.termux/files/usr/include \
  $ARQ.c \
  -o lib$ARQ.so \
  -lm

cp lib$ARQ.so $CASA/AideProjects/MiniMine/gdx-android/libs/arm64-v8a
cp mini.sh $CASA/AideProjects/MiniMine/c/
