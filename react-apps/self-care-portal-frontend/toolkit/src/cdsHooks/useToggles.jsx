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

import { useState } from 'react';

/**
 * useToggles is a custom React hook that manages the state of a toggleable component.
 * This hook does the following:
 * 1. Initializes state variables for isOpen and activeItem.
 * 2. Defines a handleToggle function that toggles the isOpen state.
 * 3. Defines a handleSelect function that sets the activeItem and toggles the isOpen state.
 * 4. Defines a handleClose function that resets isOpen and activeItem to their initial states.
 *
 * The state variables and functions can be used in a component to control the open/closed state,
 * handle the selection of an item, and close the component.
 * 
 * @param {Boolean} initialState - The initial state of the toggle (open/closed).
 * @returns {Object} - An object containing the isOpen state, activeItem state, and the handleToggle, handleSelect, and handleClose functions.
 * @example
 * const { isOpen, activeItem, handleToggle, handleSelect, handleClose } = useToggles();
 */

export const useToggles = (initialState = false) => {
  const [isOpen, setIsOpen] = useState(initialState);
  const [activeItem, setActiveItem] = useState(null);

  const handleToggle = () => {
    setIsOpen(!isOpen);
  };

  const handleSelect = (item) => {
    setActiveItem(item);
    handleToggle();
  };

  const handleClose = () => {
    setIsOpen(false);
    setActiveItem(null);
  };

  return { isOpen, activeItem, handleToggle, handleSelect, handleClose };
};
