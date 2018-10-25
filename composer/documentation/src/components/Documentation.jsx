import React from 'react';
import Description from './Description';
import './Documentation.css';

const Documentation = ({ docDetails }) => {
    const { title, description, parameters, returnParameter } = docDetails;
    return (
        <div className='documentation'>
            <div className='title'><b>{title}</b></div>
            <Description source={description} className='description' />
            {parameters.length > 0 && (
                <table className='parameters'>
                    <tbody>
                        <tr>
                            <th>Parameter Name</th>
                            <th>Data Type</th>
                            <th>Default Value</th>
                            <th>Description</th>
                        </tr>
                        {
                            parameters.map((param) => {
                                const { name, type, defaultValue, description } = param;
                                return (
                                    <tr>
                                        <td>{name}</td>
                                        <td>{type}</td>
                                        <td>{defaultValue}</td>
                                        <td>{<Description source={description} />}</td>
                                    </tr>
                                );
                            })
                        }
                    </tbody>
                </table>
            )}
            {returnParameter && returnParameter.type !== 'nil' && (
                <table className='return-parameters'>
                    <tbody>
                        <tr>
                            <th>Return Type</th>
                            <th>Description</th>
                        </tr>
                        <tr>
                            <td>{returnParameter.type}</td>
                            <td>{<Description source={returnParameter.description || ""} />}</td>
                        </tr>
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default Documentation;
