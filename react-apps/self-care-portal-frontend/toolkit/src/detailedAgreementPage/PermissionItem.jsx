/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, { useEffect, useState } from 'react';
import { Accordion, Card, Col, Container, Row } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCaretDown, faCaretUp } from '@fortawesome/free-solid-svg-icons';

import '../../../toolkit/src/css/ConsentHistoryStyles.css';

export const PermissionItem = ({ permissionScope, permissionDataLanguage }) => {
  const [showDetailedPermissions, setShowDetailedPermissions] = useState(false);
  const [filteredDataLang, setFilteredDataLang] = useState({
    scope: '',
    dataCluster: '',
    permissions: [],
  });

  useEffect(() => {
    if (permissionScope.length > 0) {
      const matchingElement = permissionDataLanguage.find(
        (element) => permissionScope === element.scope
      );
      if (matchingElement) {
        setFilteredDataLang(matchingElement);
      }
    } else {
      setFilteredDataLang(permissionDataLanguage[0]);
    }
  }, [permissionScope]);

  const toggle = () => setShowDetailedPermissions(!showDetailedPermissions);

  //must add  conditional statements for data clusters and permissions
  // when response is adjusted to receive the customer type (business, individual)

  return (
    <>
      <Accordion>
        <Card className="clusterContainer">
          <Accordion.Toggle className="clusterRow" onClick={toggle} as={Card.Header} eventKey="0">
            <Col className="clusterLabel">
              <h6>{filteredDataLang.dataCluster}</h6>
            </Col>
            <Col className="arrow">
              <FontAwesomeIcon
                className="clusToggle fa-lg"
                id="clusterToggle"
                icon={showDetailedPermissions ? faCaretUp : faCaretDown}
              />
            </Col>
          </Accordion.Toggle>
          <Accordion.Collapse eventKey="0">
            <Card.Body>
              <Container className = "cluster-card-container">
                  <Row>
                    {filteredDataLang.permissions.map((index, idx) => (
                      <Col xs={12} sm={6} md={6} lg={4} key={idx}>
                        <ul>
                          <li>{index}</li>
                        </ul>
                      </Col>
                    ))}
                  </Row>
                </Container>
              </Card.Body>
          </Accordion.Collapse>
        </Card>
      </Accordion>
    </>
  );
};
