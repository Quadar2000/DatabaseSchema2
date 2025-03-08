import styled from 'styled-components';

export const StyledForm = styled.form`
  font-weight: 700;
  font-family: var(--font-mono);
  display: flex;
  justify-content: center;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.38);
  border-radius: 12px;
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
`;

export const StyledInput = styled.input`
  width: 25ch;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.2);
  background-color: rgba(255, 255, 255, 0.38);
  font-size: 1rem;
  transition: all 0.2s;

  &:focus {
    border-color: #0071ff;
    box-shadow: 0px 0px 8px rgba(0, 113, 255, 0.4);
  }
`;


export default {StyledForm, StyledInput};