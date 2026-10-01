#!/usr/bin/env bash
set -euo pipefail

echo '=== Linux environment ==='
uname -a
cat /etc/os-release

if ! grep -q 'HADOOP_EXAMPLES' "$HOME/.bashrc"; then
    printf '\n# examples used by the grep exercise\nexport HADOOP_EXAMPLES="hadoop-mapreduce-examples"\n' >> "$HOME/.bashrc"
fi

echo '=== 1 cd ==='
cd /usr/local && pwd
cd .. && pwd
cd "$HOME" && pwd

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
cp "$HOME/.bashrc" /usr/bashrc1
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
cat "$HOME/.bashrc"

echo '=== 9 tac ==='
tac "$HOME/.bashrc" | head -n 12

echo '=== 10 more ==='
more "$HOME/.bashrc" | head -n 12

echo '=== 11 head ==='
head -n 20 "$HOME/.bashrc"
head -n -50 "$HOME/.bashrc"

echo '=== 12 tail ==='
tail -n 20 "$HOME/.bashrc"
tail -n +51 "$HOME/.bashrc"

echo '=== 13 touch ==='
touch /tmp/hello
ls -l --full-time /tmp/hello
touch -d '5 days ago' /tmp/hello
ls -l --full-time /tmp/hello

echo '=== 14 chown ==='
chown root:root /tmp/hello
ls -l /tmp/hello

echo '=== 15 find ==='
find -L "$HOME" -name .bashrc -type f

echo '=== 16 tar ==='
mkdir /test
tar -cf /test.tar -C / test
gzip -f /test.tar
gzip -dc /test.tar.gz | tar -xf - -C /tmp
find /tmp/test -maxdepth 1 -type d

echo '=== 17 grep ==='
grep -n 'examples' "$HOME/.bashrc"

echo 'ALL_LINUX_COMMANDS_EXECUTED_ON_UBUNTU'
