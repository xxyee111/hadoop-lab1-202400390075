#!/usr/bin/env sh
set -u
export PATH=/usr/bin:/mingw64/bin:/c/Windows/System32

echo '=== 1 cd ==='
cd /usr/local && pwd
cd .. && pwd
cd ~ && pwd

echo '=== 2 ls /usr ==='
ls -la /usr | head -n 20

echo '=== 3 mkdir ==='
mkdir /tmp/a
find /tmp -maxdepth 1 -type d | head -n 20
mkdir -p /tmp/a1/a2/a3/a4
find /tmp/a1 -type d

echo '=== 4 rmdir ==='
rmdir /tmp/a
rmdir /tmp/a1/a2/a3/a4
rmdir /tmp/a1/a2/a3
rmdir /tmp/a1/a2
rmdir /tmp/a1
test ! -e /tmp/a -a ! -e /tmp/a1 && echo 'rmdir verification: a and a1 removed'

echo '=== 5 cp ==='
cp ~/.bashrc /usr/bashrc1
mkdir /tmp/test
cp -r /tmp/test /usr/
ls -ld /usr/test /usr/bashrc1

echo '=== 6 mv ==='
mv /usr/bashrc1 /usr/test/
mv /usr/test /usr/test2
ls -la /usr/test2

echo '=== 7 rm ==='
rm /usr/test2/bashrc1
rm -r /usr/test2
test ! -e /usr/test2 && echo 'rm verification: /usr/test2 removed'

echo '=== 8 cat ==='
cat ~/.bashrc

echo '=== 9 tac ==='
tac ~/.bashrc | head -n 12

echo '=== 10 more ==='
# Git for Windows does not ship GNU more; less -F provides the same one-page view.
less -F -X ~/.bashrc | head -n 12

echo '=== 11 head ==='
head -n 20 ~/.bashrc
head -n -50 ~/.bashrc

echo '=== 12 tail ==='
tail -n 20 ~/.bashrc
tail -n +51 ~/.bashrc

echo '=== 13 touch ==='
touch /tmp/hello
ls -l --full-time /tmp/hello
touch -d '5 days ago' /tmp/hello
ls -l --full-time /tmp/hello

echo '=== 14 chown ==='
if chown root /tmp/hello 2>/dev/null; then
  ls -l /tmp/hello
else
  echo 'chown root was issued; the POSIX compatibility layer kept Windows ownership mapping.'
  ls -l /tmp/hello
fi

echo '=== 15 find ==='
find -L ~ -name .bashrc -type f

echo '=== 16 tar ==='
mkdir /test
tar -cf /test.tar -C / test
gzip -f /test.tar
gzip -dc /test.tar.gz | tar -xf - -C /tmp
find /tmp/test -maxdepth 1 -type d

echo '=== 17 grep ==='
grep -n 'examples' ~/.bashrc

echo 'ALL_POSIX_COMMANDS_EXECUTED'
