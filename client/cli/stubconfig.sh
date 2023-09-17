#!/bin/bash -e
#
# Copyright 2021,2023 agwlvssainokuni
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# ディレクトリ解決の起点(このスクリプトを配置したディレクトリ)。
basedir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)

######################################################################
# (1) コマンドライン引数を解析する。
usage_and_exit() {
    echo "Usage: $0 [options] {STUBCONFIG DIR}..." 1>&2
    echo "  Options" 1>&2
    echo "    -l {BASE URL}" 1>&2
    echo "    -u {BASIC AUTH}" 1>&2
    echo "    -H {HTTP HEADER}" 1>&2
    echo "    -r" 1>&2
    echo "    -c" 1>&2
    exit $1
}

# 構成項目。
conf_url=http://localhost:8080
conf_mode=show
curlcmd=(curl)

while getopts l:u:H:rch OPT
do
    case $OPT in
    l) conf_url=${OPTARG%/};;
    u) curlcmd+=(-u "${OPTARG}");;
    H) curlcmd+=(-H "${OPTARG}");;
    r) conf_mode=register;;
    c) conf_mode=clear;;
    h) usage_and_exit 0;;
    \?) usage_and_exit -1;;
    esac
done
shift $((OPTIND - 1))

######################################################################
# (2) 主処理の実体を定義する。

while [[ $# -gt 0 ]]
do
    # (a) スタブ設定ファイルをリストアップする。
    srcdir=${1%/} && shift
    echo "PROCESSING ${srcdir}"
    find "${srcdir}" -type f -name '*.js' | sort | while read file
    do
        # (b) スタブ設定対象のクラス名、メソッド名、メソッドインデックスを抽出する。
        className=$(basename $(dirname ${file}))
        basename ${file} .js | while IFS=. read name index
        do
            methodName=${name}
            methodIndex=${index:-0}
            # (c) 抽出したクラス名、メソッド名、メソッドインデックスを画面に表示する。
            echo ${file}
            echo "  ${className}"
            echo "  ${methodName} ${methodIndex}"
            # (d) スタブ設定対象APIを呼び出す。
            case $conf_mode in
            register)
                "${curlcmd[@]}" \
                    --data-urlencode "className=${className}" \
                    --data-urlencode "methodName=${methodName}" \
                    --data-urlencode "methodIndex=${methodIndex}" \
                    --data-urlencode "script@${file}" \
                    --data-urlencode "engine=" \
                    "${conf_url}/testtool/stubconfig/put"
                ;;
            clear)
                "${curlcmd[@]}" \
                    --data-urlencode "className=${className}" \
                    --data-urlencode "methodName=${methodName}" \
                    --data-urlencode "methodIndex=${methodIndex}" \
                    --data-urlencode "script=" \
                    --data-urlencode "engine=" \
                    "${conf_url}/testtool/stubconfig/put"
                ;;
            *)
                "${curlcmd[@]}" \
                    --data-urlencode "className=${className}" \
                    --data-urlencode "methodName=${methodName}" \
                    --data-urlencode "methodIndex=${methodIndex}" \
                    "${conf_url}/testtool/stubconfig/get"
                echo
                ;;
            esac
        done
    done
done
