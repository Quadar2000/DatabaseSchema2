import styled from 'styled-components';

export const StyledDiv = styled.div`
  font-weight: 700;
  font-family: var(--font-mono);
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: var(--border-radius);
  background-color: rgba(255, 255, 255, 0.38);
  flex-direction: column;
  gap: 20px;
  padding: 20px;

  &.black {
    background-color: rgba(0, 0, 0, 0.38);
    flex-direction: column;
    display: flex;
  }
`;

export const DivInForm= styled.div`
  flex-direction: column;
  display: flex;
`;


export default {StyledDiv,DivInForm};