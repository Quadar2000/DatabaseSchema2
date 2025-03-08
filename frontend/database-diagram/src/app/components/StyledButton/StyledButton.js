import React from 'react';
import styled from 'styled-components';

// Definiowanie stylowanego przycisku za pomocą styled-components
// background-color: #007BFF;
  // color: white;
  // padding: 10px 20px;
  // border: none;
  // border-radius: 5px;
  // cursor: pointer;
  // font-size: 12px;
  // &:hover {
  //   background-color: #0056b3;
  // }

  // &.custom-class {
  //   background-color: #28a745; 
  // }
const StyledButton = styled.button`
  
  padding: 12px 20px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(90deg, #0071ff, #16abff);
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease-in-out;

  &:hover {
  transform: translateY(-2px);
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.3);
}

&:active {
  transform: translateY(0px);
  box-shadow: none;
}
  
`;

export default StyledButton;