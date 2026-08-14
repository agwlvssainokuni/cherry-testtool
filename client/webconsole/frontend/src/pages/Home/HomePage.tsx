/*
 * Copyright 2023,2026 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Card } from 'make-you-chic-ui'
import { Link } from 'react-router-dom'
import './HomePage.css'

const HomePage = () => {
  return (
    <div className="home-page">
      <h1 className="home-page-title">テストツール</h1>

      <div className="home-page-cards">
        <Link to="/invoker" className="home-page-card-link" data-testid="home-card-invoker">
          <Card>
            <h2 className="home-page-card-title">呼出しツール</h2>
            <p className="home-page-card-description">
              指定したBeanのメソッドを、任意の引数を指定して直接呼び出します。
            </p>
          </Card>
        </Link>
        <Link to="/stubconfig" className="home-page-card-link" data-testid="home-card-stubconfig">
          <Card>
            <h2 className="home-page-card-title">スタブ設定ツール</h2>
            <p className="home-page-card-description">
              メソッドの戻り値をスクリプトで差し替えるスタブを設定します。
            </p>
          </Card>
        </Link>
      </div>
    </div>
  )
}

export default HomePage
