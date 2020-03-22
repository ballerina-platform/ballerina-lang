aiServiceUrlParam=$1
echo 'export REMOTE_AI_SERVICE_URL="'$aiServiceUrlParam'";' >>~/.bash_profile
source ~/.bash_profile